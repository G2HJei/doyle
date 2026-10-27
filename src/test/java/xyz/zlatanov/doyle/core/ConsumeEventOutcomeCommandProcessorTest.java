package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.command.ConsumeEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.entity.SettlementJob;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumeEventOutcomeCommandProcessorTest {

    @Mock
    SettlementJobRepository               settlementJobRepository;
    @InjectMocks
    PropagateEventOutcomeCommandProcessor service;

    ConsumeEventOutcomeCommand command = new ConsumeEventOutcomeCommand(new EventId(randomUUID()), -1);

    @Test
    void shouldCreateSettlementJob() {
        service.consumeOutcome(command);
        verify(settlementJobRepository).save(SettlementJob.restore()
                .eventId(command.eventId())
                .outcomeVersion(command.outcomeVersion())
                .build());
    }

}
