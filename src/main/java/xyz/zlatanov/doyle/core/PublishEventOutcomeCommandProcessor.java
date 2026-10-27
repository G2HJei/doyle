package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.command.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

@Slf4j
@RequiredArgsConstructor
public class PublishEventOutcomeCommandProcessor implements PublishEventOutcomeUseCase {

    private final EventOutcomeRepository repository;

    @Transactional
    @Override
    public void publish(PublishEventOutcomeCommand command) {
        val eventOutcome = repository.findOneByEventId(command.eventId())
                .orElseGet(EventOutcome::new);
        if (outcomeChanged(command, eventOutcome)) {
            eventOutcome.eventId(command.eventId())
                    .eventName(command.eventName())
                    .eventWinnerId(command.eventWinnerId())
                    .published(false);
            repository.save(eventOutcome);
            log.info("Event outcome saved: eventId={}, outcomeVersion={}, winnerId={}",
                    eventOutcome.eventId(), eventOutcome.version(), eventOutcome.eventWinnerId());
        }
    }

    private boolean outcomeChanged(PublishEventOutcomeCommand command, EventOutcome eventOutcome) {
        return !command.eventName().equals(eventOutcome.eventName())
                || !command.eventWinnerId().equals(eventOutcome.eventWinnerId());
    }
}
