package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
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
        val outcomesToRelay = outcomeRepository.findUnpublished(CHUNK_SIZE);
        for (val outcome : outcomesToRelay) {
            publisher.publish(new EventOutcomeMessage()
                    .eventId(outcome.eventId())
                    .eventName(outcome.eventName())
                    .eventWinnerId(outcome.eventWinnerId())
                    .version(outcome.version()));
            outcome.published(true);
        }
        outcomeRepository.saveAll(outcomesToRelay);
    }
}
