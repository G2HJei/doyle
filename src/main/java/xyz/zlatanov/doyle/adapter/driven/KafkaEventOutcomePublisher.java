package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.kafka.core.KafkaTemplate;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;

import static java.util.concurrent.TimeUnit.SECONDS;

@RequiredArgsConstructor
public class KafkaEventOutcomePublisher implements EventOutcomePublisher {

    private final KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate;
    private final String                                     topic;

    @Override
    @SneakyThrows
    public void publish(EventOutcomeMessage outcome) {
        val partitionKey = outcome.eventId().toString();
        kafkaTemplate.send(topic, partitionKey, outcome)
                .get(5, SECONDS);
    }
}
