package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import xyz.zlatanov.doyle.core.domain.command.ConsumeEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.message.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

import static xyz.zlatanov.doyle.config.KafkaConfig.EVENT_OUTCOMES_TOPIC;

@RequiredArgsConstructor
public class KafkaEventOutcomeListener {

    private final ConsumeEventOutcomeUseCase consumeEventOutcomeUseCase;

    @KafkaListener(topics = EVENT_OUTCOMES_TOPIC, concurrency = "3")
    public void consume(EventOutcomeMessage outcome) {
        consumeEventOutcomeUseCase.consumeOutcome(new ConsumeEventOutcomeCommand()
                .eventId(new EventId(outcome.eventId()))
                .outcomeVersion(outcome.version()));
    }
}
