package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventOutcomeServiceTest {

    @Mock
    EventOutcomePublisher publisher;
    @InjectMocks
    EventOutcomeService   service = new EventOutcomeService(publisher);

    EventOutcome outcome = new EventOutcome()
            .id(UUID.randomUUID())
            .name("Test event")
            .winnerId(UUID.randomUUID());

    @Test
    void shouldPublishEventOutcome() {
        service.publish(outcome);
        verify(publisher).publish(outcome);
    }
}