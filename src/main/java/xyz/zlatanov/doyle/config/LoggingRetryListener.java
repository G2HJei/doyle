package xyz.zlatanov.doyle.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.listener.RetryListener;

@Slf4j
public class LoggingRetryListener implements RetryListener {
    @Override
    public void failedDelivery(@NonNull ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {
        log.warn("Delivery attempt {} failed: topic={}, partition={}, offset={}, key={}", deliveryAttempt, record.topic(), record.partition(), record.offset(), record.key(), ex);
    }

    @Override
    public void recovered(@NonNull ConsumerRecord<?, ?> record, Exception ex) {
        log.error("Record sent to dead letter topic: topic={}, partition={}, offset={}, key={}", record.topic(), record.partition(), record.offset(), record.key());
    }

}
