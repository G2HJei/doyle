package xyz.zlatanov.doyle.core;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.entity.SettlementJob;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.UserId;
import xyz.zlatanov.doyle.core.domain.message.BetSettlementMessage;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;

import java.util.Arrays;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    SettlementJob settlementJob = new SettlementJob().eventId(new EventId(randomUUID()));
    Bet           bet           = getBet();

    @BeforeEach
    void setUp() {
        when(settlementJobRepository.nextPending()).thenReturn(Optional.of(settlementJob));
    }

    @Test
    void shouldDoNothingWhenNoPendingJobs() {
        when(settlementJobRepository.nextPending()).thenReturn(Optional.empty());
        commandProcessor.match();
        verify(publisher, never()).publish(any());
        verify(settlementJobRepository, never()).save(any());
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
    void shouldReportNothingMatchedWhenNoPendingJobs() {
        when(settlementJobRepository.nextPending()).thenReturn(Optional.empty());
        assertFalse(commandProcessor.match());
    }

    @Test
    void shouldReportMatchedWhenBetSettlementsSent() {
        betsToSettle(bet);
        assertTrue(commandProcessor.match());
    }

    @Test
    void shouldReportMatchedWhenJobCompleted() {
        assertTrue(commandProcessor.match()); // another job may be pending
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
                .id(new BetId(randomUUID()))
                .eventId(settlementJob.eventId())
                .userId(new UserId(randomUUID()));
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
            verify(publisher).publish(new BetSettlementMessage(b));
        }
    }
}