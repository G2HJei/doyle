package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.kafka.core.KafkaTemplate;
import xyz.zlatanov.doyle.core.domain.message.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RequiredArgsConstructor
public class KafkaEventOutcomePublisher implements EventOutcomePublisher {

    private final KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate;
    private final String                                     topic;
    private final Duration                                   sendTimeout;

    @Override
    @SneakyThrows
    public void publish(EventOutcomeMessage message) {
        val partitionKey = message.eventId().toString();
        kafkaTemplate.send(topic, partitionKey, message)
                .get(sendTimeout.toMillis(), MILLISECONDS);
    }
}
