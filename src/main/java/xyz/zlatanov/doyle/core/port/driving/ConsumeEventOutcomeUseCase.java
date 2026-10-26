package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.EventOutcome;

public interface ConsumeEventOutcomeUseCase {

    void consumeOutcome(EventOutcome outcome);
}
