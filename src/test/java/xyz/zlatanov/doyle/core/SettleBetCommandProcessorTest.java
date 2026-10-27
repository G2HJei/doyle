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

    EventOutcome outcome           = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventWinnerId(UUID.randomUUID())
            .version(1);
    Bet          losingBet         = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(UUID.randomUUID()) //not the winner
            .status(PLACED);
    Bet          winningBet        = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(outcome.eventWinnerId())
            .amount(ONE)
            .status(PLACED);
    Bet          settledWinningBet = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(outcome.eventWinnerId())
            .amount(ONE)
            .status(SETTLED)
            .settledEventVersion(1);

    @BeforeEach
    void setUp() {
        when(eventOutcomeLookup.getEventOutcome(outcome.eventId())).thenReturn(outcome);
    }

    @Test
    void shouldSettleBetAsLost() {
        when(betRepository.findById(losingBet.id())).thenReturn(losingBet);

        commandProcessor.settle(new SettleBetCommand().betId(losingBet.id()));

        verify(betRepository).updateStatusAndResult(losingBet.id(), SETTLED, LOST);
        verifyNoInteractions(betSettlementRepository);
    }

    @Test
    void shouldSettleWinningBet() {
        when(betRepository.findById(winningBet.id())).thenReturn(winningBet);

        commandProcessor.settle(new SettleBetCommand().betId(winningBet.id()));

        verify(betRepository).updateStatusAndResult(winningBet.id(), SETTLED, WON);
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
        verifyNoInteractions(betRepository, betSettlementRepository);
    }
}