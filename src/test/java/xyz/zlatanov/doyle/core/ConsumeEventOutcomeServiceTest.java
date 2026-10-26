package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumeEventOutcomeServiceTest {

    @Mock
    SettlementJobRepository    settlementJobRepository;
    @InjectMocks
    ConsumeEventOutcomeService service;

    EventOutcome outcome = new EventOutcome()
            .eventId(UUID.randomUUID())
            .version(-1);

    @Test
    void shouldCreateSettlementJob() {
        service.consumeOutcome(outcome);
        verify(settlementJobRepository).createJob(outcome.eventId(), outcome.version());
    }

}