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
        repository.findOneByEventId(command.eventId())
                .ifPresentOrElse(eventOutcome -> correct(eventOutcome, command), () -> create(command));
    }

    private void create(PublishEventOutcomeCommand command) {
        val eventOutcome = EventOutcome.create(command.eventId(), command.eventName(), command.eventWinnerId());
        save(eventOutcome);
    }

    private void correct(EventOutcome eventOutcome, PublishEventOutcomeCommand command) {
        if (outcomeChanged(command, eventOutcome)) {
            eventOutcome.correct(command.eventName(), command.eventWinnerId());
            save(eventOutcome);
        }
    }

    private void save(EventOutcome eventOutcome) {
        repository.save(eventOutcome);
        log.info("Event outcome saved: eventId={}, outcomeVersion={}, winnerId={}",
                eventOutcome.eventId(), eventOutcome.version(), eventOutcome.eventWinnerId());
    }

    private boolean outcomeChanged(PublishEventOutcomeCommand command, EventOutcome eventOutcome) {
        return !command.eventName().equals(eventOutcome.eventName())
                || !command.eventWinnerId().equals(eventOutcome.eventWinnerId());
    }
}
