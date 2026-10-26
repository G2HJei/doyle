package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

@RequiredArgsConstructor
public class EventOutcomeService implements PublishEventOutcomeUseCase {

    private final EventOutcomePublisher eventOutcomePublisher;

    public void publish(EventOutcome outcome) {

    }
}
