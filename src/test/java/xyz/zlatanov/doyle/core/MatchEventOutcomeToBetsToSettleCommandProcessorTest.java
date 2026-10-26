package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventOutcomeToBetsToSettleCommandProcessorTest {

    @Mock
    SettlementJobRepository                         settlementJobRepository;
    @Mock
    BetRepository                                   betRepository;
    @Mock
    BetSettlementPublisher                          publisher;
    @InjectMocks
    MatchEventOutcomeToBetsToSettleCommandProcessor commandProcessor;

    SettlementJob settlementJob = new SettlementJob()
            .eventId(UUID.randomUUID())
            .outcomeVersion(1);
    Bet           bet           = new Bet()
            .id(UUID.randomUUID())
            .eventId(settlementJob.eventId())
            .userId(UUID.randomUUID());

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

    @Test
    void shouldPublishBetSettlements() {
        when(settlementJobRepository.nextPending()).thenReturn(settlementJob);
        when(betRepository.findByEventIdAndAfterIdAndLimit(settlementJob.eventId(), null, 100)).thenReturn(List.of(bet));

        commandProcessor.match();

        verify(publisher).publish(new BetSettlementMessage().betId(bet.id()));
    }

    @Test
    void shouldNotCompleteJobWhenBetsWereSettled() {
        when(settlementJobRepository.nextPending()).thenReturn(settlementJob);
        when(betRepository.findByEventIdAndAfterIdAndLimit(settlementJob.eventId(), null, 100)).thenReturn(List.of(bet));
        commandProcessor.match();
        verify(settlementJobRepository, never()).markCompleted(any());
    }
}