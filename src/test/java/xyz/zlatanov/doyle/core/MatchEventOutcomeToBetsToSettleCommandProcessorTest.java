package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventOutcomeToBetsToSettleCommandProcessorTest {

    @Mock
    SettlementJobRepository                         settlementJobRepository;
    @Mock
    BetSettlementPublisher                          publisher;
    @InjectMocks
    MatchEventOutcomeToBetsToSettleCommandProcessor commandProcessor;

    SettlementJob settlementJob = new SettlementJob()
            .eventId(UUID.randomUUID())
            .outcomeVersion(1);

    @Test
    void shouldDoNothingWhenNoPendingJobs() {
        commandProcessor.match();
        verify(publisher, never()).publish(any());
    }

    @Test
    void shouldCompleteJobWhenNoBetsToSettle() {
        when(settlementJobRepository.nextPending()).thenReturn(settlementJob);
        commandProcessor.match();
        verify(settlementJobRepository).markCompleted(settlementJob.id());
    }
}