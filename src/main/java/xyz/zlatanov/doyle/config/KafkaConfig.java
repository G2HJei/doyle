package xyz.zlatanov.doyle.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import xyz.zlatanov.doyle.adapter.driven.KafkaEventOutcomePublisher;
import xyz.zlatanov.doyle.adapter.driving.KafkaEventOutcomeListener;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@Configuration
public class KafkaConfig {

    public static final String EVENT_OUTCOMES_TOPIC = "event-outcomes";

    @Bean
    NewTopic eventOutcomesTopic() {
        return TopicBuilder.name(EVENT_OUTCOMES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    EventOutcomePublisher eventOutcomePublisher(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate) {
        return new KafkaEventOutcomePublisher(kafkaTemplate, EVENT_OUTCOMES_TOPIC);
    }

    @Bean
    KafkaEventOutcomeListener kafkaEventOutcomeListener(ConsumeEventOutcomeUseCase consumeEventOutcomeUseCase) {
        return new KafkaEventOutcomeListener(consumeEventOutcomeUseCase);
    }
}
