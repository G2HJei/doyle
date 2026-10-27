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
        val won = Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId());
        val result = won ? WON : LOST;
        val resultChanged = result == bet.result();
        betRepository.updateSettlementData(bet.id(), SETTLED, result, eventOutcome.version());
        if (!won && PLACED == bet.status()) {
            return; // nothing to pay out
        }
        if (resultChanged) {
            return; // result unchanged, already settled
        }
        betSettlementRepository.save(new BetSettlement()
                .betId(bet.id())
                .settledEventVersion(eventOutcome.version())
                .deltaAmount(deltaAmount(bet, won))
                .payoutStatus(PENDING));
    }

    private boolean outcomeAlreadySettled(Bet bet, EventOutcome eventOutcome) {
        return bet.settledEventVersion() >= eventOutcome.version();
    }

    // odds not included in the model, so assumption is the odds are always 2 and the bet amount is equal to the payout
    private BigDecimal deltaAmount(Bet bet, boolean won) {
        val revokedWin = !won && WON == bet.result();
        return revokedWin ? bet.amount().multiply(new BigDecimal("-2")) : bet.amount();
    }
}
