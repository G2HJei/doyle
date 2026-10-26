package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;

public interface EventOutcomePublisher {
    void publish(EventOutcomeMessage outcome);
}
