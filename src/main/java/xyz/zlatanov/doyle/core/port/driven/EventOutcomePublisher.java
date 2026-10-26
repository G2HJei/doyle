package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventOutcome;

public interface EventOutcomePublisher {
    void publish(EventOutcome outcome);
}
