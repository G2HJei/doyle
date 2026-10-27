package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.message.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.RelayEventOutcomesUseCase;

@Slf4j
@RequiredArgsConstructor
public class RelayEventOutcomesCommandProcessor implements RelayEventOutcomesUseCase {

    private final EventOutcomeRepository outcomeRepository;
    private final EventOutcomePublisher  publisher;
    private final int                    chunkSize;

    @Transactional
    @Override
    public void relayOutcomes() {
        val outcomes = outcomeRepository.findUnpublished(chunkSize);
        outcomes.forEach(this::publish);
        outcomeRepository.markPublished(outcomes);
    }

    private void publish(EventOutcome outcome) {
        publisher.publish(new EventOutcomeMessage(outcome));
        log.info("Event outcome relayed: eventId={}, outcomeVersion={}", outcome.eventId(), outcome.version());
    }
}
