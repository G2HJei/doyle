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
    public void publish(EventOutcomeMessage message) {
        val partitionKey = message.eventId().toString();
        kafkaTemplate.send(topic, partitionKey, message)
                .get(5, SECONDS);
    }
}
