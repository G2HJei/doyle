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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishEventOutcomeCommandProcessorTest {

    @Mock
    EventOutcomeRepository              repository;
    @InjectMocks
    PublishEventOutcomeCommandProcessor service;

    PublishEventOutcomeCommand command = new PublishEventOutcomeCommand()
            .eventId(UUID.randomUUID())
            .eventName("Victoria-Levski")
            .eventWinnerId(UUID.randomUUID());

    @Test
    void shouldPublishNewEventOutcome() {
        service.publish(command);
        verify(repository).save(eventOutcome(null));
    }

    @Test
    void shouldPublishUpdatedEventOutcome() {
        when(repository.findOneByEventId(command.eventId()))
                .thenReturn(eventOutcome(1, UUID.randomUUID()));
        service.publish(command);
        verify(repository).save(eventOutcome(1));
    }

    @Test
    void shouldNotPublishUnchangedEventOutcome() {
        when(repository.findOneByEventId(command.eventId()))
                .thenReturn(eventOutcome(1, UUID.randomUUID()));
        service.publish(command);

        service.publish(command);

        verify(repository).save(eventOutcome(1));
    }

    EventOutcome eventOutcome(Integer version) {
        return eventOutcome(version, command.eventWinnerId());
    }

    EventOutcome eventOutcome(Integer version, UUID winnerId) {
        return new EventOutcome()
                .eventId(command.eventId())
                .eventName(command.eventName())
                .eventWinnerId(winnerId)
                .published(false)
                .version(version);
    }
}