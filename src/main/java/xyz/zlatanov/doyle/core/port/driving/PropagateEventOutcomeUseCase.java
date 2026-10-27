package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.command.ConsumeEventOutcomeCommand;

public interface PropagateEventOutcomeUseCase {

    void consumeOutcome(ConsumeEventOutcomeCommand command);
}
