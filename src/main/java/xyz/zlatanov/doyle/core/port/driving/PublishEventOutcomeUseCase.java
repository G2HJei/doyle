package xyz.zlatanov.doyle.core.port.driving;

import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;

public interface PublishEventOutcomeUseCase {
    @Transactional
    void publish(PublishEventOutcomeCommand command);
}
