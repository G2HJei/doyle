package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

@RequiredArgsConstructor
public class PublishEventOutcomeService implements PublishEventOutcomeUseCase {

    private final EventOutcomePublisher publisher;

    public void publish(PublishEventOutcomeCommand command) {
        //atm every update gets published, version? no-op? outbox?
        publisher.publish(new EventOutcome()
                .eventId(command.eventId())
                .eventName(command.eventName())
                .eventWinnerId(command.eventWinnerId())
                .version(1));
    }
}
