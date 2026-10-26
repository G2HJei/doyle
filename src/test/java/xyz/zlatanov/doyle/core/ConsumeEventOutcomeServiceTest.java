package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumeEventOutcomeServiceTest {

    @Mock
    SettlementJobRepository    settlementJobRepository;
    @InjectMocks
    ConsumeEventOutcomeService service;

    EventOutcomeMessage outcome = new EventOutcomeMessage()
            .eventId(UUID.randomUUID())
            .version(-1);

    @Test
    void shouldCreateSettlementJob() {
        service.consumeOutcome(outcome);
        verify(settlementJobRepository).save(new SettlementJob()
                .eventId(outcome.eventId())
                .outcomeVersion(outcome.version()));
    }

}