package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.command.PublishEventOutcomeCommand;

public interface PublishEventOutcomeUseCase {
    void publish(PublishEventOutcomeCommand command);
}
