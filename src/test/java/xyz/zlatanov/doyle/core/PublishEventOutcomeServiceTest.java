package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublishEventOutcomeServiceTest {

    @Mock
    EventOutcomePublisher      publisher;
    @InjectMocks
    PublishEventOutcomeService service;

    EventOutcome outcome = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventName("Test event")
            .eventWinnerId(UUID.randomUUID());

    @Test
    void shouldPublishEventOutcome() {
        service.publish(outcome);
        verify(publisher).publish(outcome);
    }
}