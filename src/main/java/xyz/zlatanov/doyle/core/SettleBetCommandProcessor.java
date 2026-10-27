package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetSettlement;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

import java.math.BigDecimal;
import java.util.Objects;

import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.BetSettlement.PayoutStatus.PENDING;

@RequiredArgsConstructor
public class SettleBetCommandProcessor implements SettleBetUseCase {

    private final EventOutcomeLookup      eventOutcomeLookup;
    private final BetRepository           betRepository;
    private final BetSettlementRepository betSettlementRepository;

    @Override
    public void settle(SettleBetCommand command) {
        val bet = betRepository.findById(command.betId());
        val eventOutcome = eventOutcomeLookup.getEventOutcome(bet.eventId());
        if (outcomeAlreadySettled(bet, eventOutcome)) {
            return;
        }
        val newResult = Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId()) ? WON : LOST;
        val oldResult = bet.result();
        val firstSettle = PLACED == bet.status();
        betRepository.save(bet
                .status(SETTLED)
                .result(newResult)
                .settledEventVersion(eventOutcome.version()));
        if (firstSettle && LOST == newResult) {
            return; // nothing to pay out
        }
        val resultChanged = newResult != oldResult;
        if (resultChanged) {
            betSettlementRepository.save(new BetSettlement()
                    .betId(bet.id())
                    .settledEventVersion(eventOutcome.version())
                    .deltaAmount(deltaAmount(oldResult, newResult, bet.amount()))
                    .payoutStatus(PENDING));
        }
    }

    private boolean outcomeAlreadySettled(Bet bet, EventOutcome eventOutcome) {
        return bet.settledEventVersion() >= eventOutcome.version();
    }

    // odds not included in the model, so assumption is the odds are always 2 and the bet amount is equal to the payout
    private BigDecimal deltaAmount(Bet.BetResult oldResult, Bet.BetResult newResult, BigDecimal amount) {
        val revokedWin = LOST == newResult && WON == oldResult;
        return revokedWin ? amount.multiply(new BigDecimal("-2")) : amount;
    }
}
