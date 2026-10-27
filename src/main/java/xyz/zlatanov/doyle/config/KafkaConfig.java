package xyz.zlatanov.doyle.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
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
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

import java.util.LinkedHashMap;

@Slf4j
@Configuration
public class KafkaConfig {

    public static final String EVENT_OUTCOMES_TOPIC     = "event-outcomes";
    public static final String DLT_SUFFIX               = "-dlt";
    public static final String EVENT_OUTCOMES_DLT_TOPIC = EVENT_OUTCOMES_TOPIC + DLT_SUFFIX;

    private static final int    MAX_RETRIES                  = 3;
    private static final long   INITIAL_RETRY_BACKOFF_MILLIS = 1000;
    private static final double RETRY_BACKOFF_MULTIPLIER     = 2;

    @Bean
    NewTopic eventOutcomesTopic() {
        return getTopic(EVENT_OUTCOMES_TOPIC);
    }

    @Bean
    NewTopic eventOutcomesDeadLetterTopic() {
        return getTopic(EVENT_OUTCOMES_DLT_TOPIC);
    }

    @Bean
    EventOutcomePublisher eventOutcomePublisher(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate) {
        return new KafkaEventOutcomePublisher(kafkaTemplate, EVENT_OUTCOMES_TOPIC);
    }

    @Bean
    KafkaEventOutcomeListener kafkaEventOutcomeListener(ConsumeEventOutcomeUseCase consumeEventOutcomeUseCase) {
        return new KafkaEventOutcomeListener(consumeEventOutcomeUseCase);
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate) {
        val errorHandler = getErrorHandler(kafkaTemplate);
        errorHandler.setRetryListeners(new LoggingRetryListener());
        return errorHandler;
    }

    private NewTopic getTopic(String eventOutcomesTopic) {
        return TopicBuilder.name(eventOutcomesTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    private DefaultErrorHandler getErrorHandler(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate) {
        val backOff = new ExponentialBackOff(INITIAL_RETRY_BACKOFF_MILLIS, RETRY_BACKOFF_MULTIPLIER);
        backOff.setMaxAttempts(MAX_RETRIES);

        val templates = new LinkedHashMap<Class<?>, KafkaOperations<?, ?>>();
        templates.put(Object.class, kafkaTemplate);
        val recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, e) -> new TopicPartition(record.topic() + DLT_SUFFIX, record.partition()));

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
