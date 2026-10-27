package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.ConsumeEventOutcomeCommand;

public interface ConsumeEventOutcomeUseCase {

    void consumeOutcome(ConsumeEventOutcomeCommand command);
}
