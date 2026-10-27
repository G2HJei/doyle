package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetSettlement;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;

import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static org.mockito.Mockito.*;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.BetSettlement.PayoutStatus.PENDING;

@ExtendWith(MockitoExtension.class)
class SettleBetCommandProcessorTest {

    @Mock
    EventOutcomeLookup      eventOutcomeLookup;
    @Mock
    BetRepository           betRepository;
    @Mock
    BetSettlementRepository betSettlementRepository;

    @InjectMocks
    SettleBetCommandProcessor commandProcessor;

    EventOutcome outcome             = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventWinnerId(UUID.randomUUID())
            .version(1);
    Bet          losingBet           = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(UUID.randomUUID()) //not the winner
            .status(PLACED);
    Bet          winningBet          = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(outcome.eventWinnerId())
            .amount(ONE)
            .status(PLACED);
    Bet          settledWinningBet   = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(outcome.eventWinnerId())
            .amount(ONE)
            .status(SETTLED)
            .result(WON)
            .settledEventVersion(1);
    Bet          correctionBet       = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(UUID.randomUUID())
            .amount(ONE)
            .status(SETTLED)
            .result(WON)
            .settledEventVersion(0);
    Bet          correctionLosingBet = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(outcome.eventWinnerId())
            .amount(ONE)
            .status(SETTLED)
            .result(LOST)
            .settledEventVersion(0);
    Bet          outdatedWinningBet  = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(outcome.eventWinnerId())
            .amount(ONE)
            .status(SETTLED)
            .result(WON)
            .settledEventVersion(0);
    Bet          outdatedLosingBet   = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(UUID.randomUUID())
            .amount(ONE)
            .status(SETTLED)
            .result(LOST)
            .settledEventVersion(0);

    @BeforeEach
    void setUp() {
        when(eventOutcomeLookup.getEventOutcome(outcome.eventId())).thenReturn(outcome);
    }

    @Test
    void shouldSettleBetAsLost() {
        when(betRepository.findById(losingBet.id())).thenReturn(losingBet);

        commandProcessor.settle(new SettleBetCommand().betId(losingBet.id()));

        verify(betRepository).updateSettlementData(losingBet.id(), SETTLED, LOST, 1);
        verifyNoInteractions(betSettlementRepository);
    }

    @Test
    void shouldSettleWinningBet() {
        when(betRepository.findById(winningBet.id())).thenReturn(winningBet);

        commandProcessor.settle(new SettleBetCommand().betId(winningBet.id()));

        verify(betRepository).updateSettlementData(winningBet.id(), SETTLED, WON, 1);
        verify(betSettlementRepository).save(new BetSettlement()
                .betId(winningBet.id())
                .settledEventVersion(1)
                .deltaAmount(ONE)
                .payoutStatus(PENDING));
    }

    @Test
    void shouldNoOpAlreadySettledBet() {
        when(betRepository.findById(settledWinningBet.id())).thenReturn(settledWinningBet);

        commandProcessor.settle(new SettleBetCommand().betId(settledWinningBet.id()));

        verify(betRepository, never()).updateSettlementData(any(), any(), any(), anyInt());
        verifyNoInteractions(betSettlementRepository);
    }

    @Test
    void shouldResettleOnWinnerChanged() {
        when(betRepository.findById(correctionBet.id())).thenReturn(correctionBet);

        commandProcessor.settle(new SettleBetCommand().betId(correctionBet.id()));

        verify(betRepository).updateSettlementData(correctionBet.id(), SETTLED, LOST, 1);
        verify(betSettlementRepository).save(new BetSettlement()
                .betId(correctionBet.id())
                .settledEventVersion(1)
                .deltaAmount(new BigDecimal("-2"))
                .payoutStatus(PENDING)
        );
    }

    @Test
    void shouldSettleOnWinnerChanged() {
        when(betRepository.findById(correctionLosingBet.id())).thenReturn(correctionLosingBet);

        commandProcessor.settle(new SettleBetCommand().betId(correctionLosingBet.id()));

        verify(betRepository).updateSettlementData(correctionLosingBet.id(), SETTLED, WON, 1);
        verify(betSettlementRepository).save(new BetSettlement()
                .betId(correctionLosingBet.id())
                .settledEventVersion(1)
                .deltaAmount(ONE)
                .payoutStatus(PENDING)
        );
    }

    @Test
    void shouldNotPayOutAgainOnUnchangedWinner() {
        when(betRepository.findById(outdatedWinningBet.id())).thenReturn(outdatedWinningBet);

        commandProcessor.settle(new SettleBetCommand().betId(outdatedWinningBet.id()));

        verify(betRepository).updateSettlementData(outdatedWinningBet.id(), SETTLED, WON, 1);
        verifyNoInteractions(betSettlementRepository);
    }

    @Test
    void shouldNotPayOutOnUnchangedLoss() {
        when(betRepository.findById(outdatedLosingBet.id())).thenReturn(outdatedLosingBet);

        commandProcessor.settle(new SettleBetCommand().betId(outdatedLosingBet.id()));

        verify(betRepository).updateSettlementData(outdatedLosingBet.id(), SETTLED, LOST, 1);
        verifyNoInteractions(betSettlementRepository);
    }
}