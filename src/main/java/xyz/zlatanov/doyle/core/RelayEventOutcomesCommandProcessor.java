package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.RelayEventOutcomesUseCase;

@RequiredArgsConstructor
public class RelayEventOutcomesCommandProcessor implements RelayEventOutcomesUseCase {

    private static final int CHUNK_SIZE = 100;

    private final EventOutcomeRepository outcomeRepository;
    private final EventOutcomePublisher  publisher;

    @Override
    public void relayOutcomes() {
        val outcomes = outcomeRepository.findUnpublished(CHUNK_SIZE);
        outcomes.forEach(this::publish);
        outcomeRepository.markPublished(outcomes);
    }

    private void publish(EventOutcome outcome) {
        publisher.publish(new EventOutcomeMessage()
                .eventId(outcome.eventId())
                .eventName(outcome.eventName())
                .eventWinnerId(outcome.eventWinnerId())
                .version(outcome.version()));
    }
}
