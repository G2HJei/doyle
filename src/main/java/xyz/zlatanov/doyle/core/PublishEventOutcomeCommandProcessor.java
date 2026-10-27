package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PublishEventOutcomeCommandProcessor implements PublishEventOutcomeUseCase {

    private final EventOutcomeRepository repository;

    @Transactional
    @Override
    public void publish(PublishEventOutcomeCommand command) {
        val eventOutcome = Optional.ofNullable(repository.findOneByEventId(command.eventId()))
                .orElseGet(EventOutcome::new);
        repository.save(eventOutcome
                .eventId(command.eventId())
                .eventName(command.eventName())
                .eventWinnerId(command.eventWinnerId())
                .published(false));
        log.info("Event outcome saved: eventId={}, outcomeVersion={}, winnerId={}",
                eventOutcome.eventId(), eventOutcome.version(), eventOutcome.eventWinnerId());
    }
}
