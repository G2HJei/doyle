package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;

public interface ConsumeEventOutcomeUseCase {

    void consumeOutcome(EventOutcomeMessage outcome);
}
