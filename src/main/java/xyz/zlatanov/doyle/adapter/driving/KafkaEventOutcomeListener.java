package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import xyz.zlatanov.doyle.core.domain.command.ConsumeEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.message.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driving.PropagateEventOutcomeUseCase;

import static xyz.zlatanov.doyle.config.KafkaConfig.EVENT_OUTCOMES_TOPIC;

@RequiredArgsConstructor
public class KafkaEventOutcomeListener {

    private final PropagateEventOutcomeUseCase propagateEventOutcomeUseCase;

    @KafkaListener(topics = EVENT_OUTCOMES_TOPIC)
    public void consume(EventOutcomeMessage outcome) {
        propagateEventOutcomeUseCase.consumeOutcome(
                new ConsumeEventOutcomeCommand(new EventId(outcome.eventId()), outcome.version()));
    }
}
