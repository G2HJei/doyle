package xyz.zlatanov.doyle.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import xyz.zlatanov.doyle.adapter.driven.KafkaEventOutcomePublisher;
import xyz.zlatanov.doyle.adapter.driving.KafkaEventOutcomeListener;
import xyz.zlatanov.doyle.core.domain.message.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driving.PropagateEventOutcomeUseCase;

import java.time.Duration;
import java.util.LinkedHashMap;

@Slf4j
@Configuration
public class KafkaConfig {

    public static final String EVENT_OUTCOMES_TOPIC     = "event-outcomes";
    public static final String DLT_SUFFIX               = "-dlt";
    public static final String EVENT_OUTCOMES_DLT_TOPIC = EVENT_OUTCOMES_TOPIC + DLT_SUFFIX;

    @Bean
    NewTopic eventOutcomesTopic(@Value("${doyle.kafka.topic-partitions}") int partitions,
                                @Value("${doyle.kafka.topic-replicas}") int replicas) {
        return getTopic(EVENT_OUTCOMES_TOPIC, partitions, replicas);
    }

    @Bean
    NewTopic eventOutcomesDeadLetterTopic(@Value("${doyle.kafka.topic-partitions}") int partitions,
                                          @Value("${doyle.kafka.topic-replicas}") int replicas) {
        return getTopic(EVENT_OUTCOMES_DLT_TOPIC, partitions, replicas);
    }

    @Bean
    EventOutcomePublisher eventOutcomePublisher(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate,
                                                @Value("${doyle.kafka.send-timeout}") Duration sendTimeout) {
        return new KafkaEventOutcomePublisher(kafkaTemplate, EVENT_OUTCOMES_TOPIC, sendTimeout);
    }

    @Bean
    KafkaEventOutcomeListener kafkaEventOutcomeListener(PropagateEventOutcomeUseCase propagateEventOutcomeUseCase) {
        return new KafkaEventOutcomeListener(propagateEventOutcomeUseCase);
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate,
                                          @Value("${doyle.kafka.retry.max-retries}") int maxRetries,
                                          @Value("${doyle.kafka.retry.initial-backoff}") Duration initialBackoff,
                                          @Value("${doyle.kafka.retry.backoff-multiplier}") double backoffMultiplier) {
        val errorHandler = getErrorHandler(kafkaTemplate, maxRetries, initialBackoff, backoffMultiplier);
        errorHandler.setRetryListeners(new LoggingRetryListener());
        return errorHandler;
    }

    private NewTopic getTopic(String eventOutcomesTopic, int partitions, int replicas) {
        return TopicBuilder.name(eventOutcomesTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    private DefaultErrorHandler getErrorHandler(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate,
                                                int maxRetries, Duration initialBackoff, double backoffMultiplier) {
        val backOff = new ExponentialBackOff(initialBackoff.toMillis(), backoffMultiplier);
        backOff.setMaxAttempts(maxRetries);

        val templates = new LinkedHashMap<Class<?>, KafkaOperations<?, ?>>();
        templates.put(Object.class, kafkaTemplate);
        val recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, e) -> new TopicPartition(record.topic() + DLT_SUFFIX, record.partition()));

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
