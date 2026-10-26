package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublishEventOutcomeServiceTest {

    @Mock
    EventOutcomeRepository     repository;
    @InjectMocks
    PublishEventOutcomeService service;

    PublishEventOutcomeCommand command = new PublishEventOutcomeCommand()
            .eventId(UUID.randomUUID())
            .eventName("Victoria-Levski")
            .eventWinnerId(UUID.randomUUID());

    @Test
    void shouldPublishNewEventOutcome() {
        service.publish(command);
        verify(repository).save(expectedOutcome(1));
    }

    EventOutcome expectedOutcome(int expectedVersion) {
        return new EventOutcome()
                .eventId(command.eventId())
                .eventName(command.eventName())
                .eventWinnerId(command.eventWinnerId())
                .published(false)
                .version(expectedVersion);
    }
}