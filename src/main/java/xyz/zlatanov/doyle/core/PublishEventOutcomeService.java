package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

@RequiredArgsConstructor
public class PublishEventOutcomeService implements PublishEventOutcomeUseCase {

    private final EventOutcomePublisher publisher;

    public void publish(EventOutcome outcome) {
        //atm every update gets published, version? no-op? outbox?
        publisher.publish(outcome);
    }
}
