package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumeEventOutcomeServiceTest {

    @Mock
    UUIDService                uuids;
    @Mock
    SettlementJobRepository    settlementJobRepository;
    @InjectMocks
    ConsumeEventOutcomeService service;

    EventOutcome  outcome       = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventName("Test event")
            .eventWinnerId(UUID.randomUUID());
    UUID          jobId         = UUID.randomUUID();
    SettlementJob settlementJob = new SettlementJob()
            .id(jobId)
            .eventId(outcome.eventId());

    @Test
    void shouldCreateSettlementJob() {
        when(uuids.get()).thenReturn(jobId);
        service.consumeOutcome(outcome);
        verify(settlementJobRepository).save(settlementJob);
    }

}