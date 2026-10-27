package xyz.zlatanov.doyle.core;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static xyz.zlatanov.doyle.core.MatchEventOutcomeToBetsToSettleCommandProcessor.CHUNK_SIZE;

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

    SettlementJob settlementJob = new SettlementJob().eventId(UUID.randomUUID());
    Bet           bet           = getBet();

    @BeforeEach
    void setUp() {
        when(settlementJobRepository.nextPending()).thenReturn(settlementJob);
    }

    @Test
    void shouldDoNothingWhenNoPendingJobs() {
        commandProcessor.match();
        verify(publisher, never()).publish(any());
    }

    @Test
    void shouldCompleteJobWhenNoBetsToSettle() {
        commandProcessor.match();
        verifyJobCompleted(true);
    }

    @Test
    void shouldNotCompleteJobWhenBetsWereSettled() {
        betsToSettle(bet);
        commandProcessor.match();
        verifyJobCompleted(false);
    }

    @Test
    void shouldPublishBetSettlements() {
        betsToSettle(bet);
        commandProcessor.match();
        verifySettlementsPublished(bet);
    }

    @Test
    void shouldPublishBetSettlementsForMultipleBets() {
        val otherBet = getBet();
        betsToSettle(bet, otherBet);

        commandProcessor.match();

        verifySettlementsPublished(bet, otherBet);
    }

    @Test
    void shouldTrackLastBetId() {
        betsToSettle(bet);
        commandProcessor.match();
        verify(settlementJobRepository).save(
                new SettlementJob()
                        .eventId(settlementJob.eventId())
                        .lastBetId(bet.id()));
    }

    Bet getBet() {
        return new Bet()
                .id(UUID.randomUUID())
                .eventId(settlementJob.eventId())
                .userId(UUID.randomUUID());
    }

    void betsToSettle(Bet... bets) {
        when(betRepository.findByEventIdAndAfterIdAndLimit(settlementJob.eventId(), null, CHUNK_SIZE))
                .thenReturn(Arrays.stream(bets).toList());
    }

    void verifyJobCompleted(boolean completed) {
        if (completed) {
            verify(settlementJobRepository).save(new SettlementJob()
                    .eventId(settlementJob.eventId())
                    .completed(true));
        } else {
            verify(settlementJobRepository, never()).save(argThat(SettlementJob::completed));
        }
    }

    void verifySettlementsPublished(Bet... bets) {
        for (val b : bets) {
            verify(publisher).publish(new BetSettlementMessage().betId(b.id()).userId(b.userId()));
        }
    }
}