package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.ConsumeEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumeEventOutcomeServiceTest {

    @Mock
    SettlementJobRepository    settlementJobRepository;
    @InjectMocks
    ConsumeEventOutcomeService service;

    ConsumeEventOutcomeCommand command = new ConsumeEventOutcomeCommand()
            .eventId(new EventId(randomUUID()))
            .outcomeVersion(-1);

    @Test
    void shouldCreateSettlementJob() {
        service.consumeOutcome(command);
        verify(settlementJobRepository).save(new SettlementJob()
                .eventId(command.eventId())
                .outcomeVersion(command.outcomeVersion()));
    }

}
