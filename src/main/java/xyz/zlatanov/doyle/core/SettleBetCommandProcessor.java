package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetSettlement;
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
        if (bet.settledEventVersion() >= eventOutcome.version()) {
            return; // already settled against this outcome version
        }
        val won = Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId());
        betRepository.updateSettlementData(bet.id(), SETTLED, won ? WON : LOST, eventOutcome.version());
        if (!won && PLACED == bet.status()) {
            return; // nothing to pay out
        }
        if (won && WON == bet.result()) {
            return; // already paid out
        }
        betSettlementRepository.save(new BetSettlement()
                .betId(bet.id())
                .settledEventVersion(eventOutcome.version())
                .deltaAmount(deltaAmount(bet, won))
                .payoutStatus(PENDING));
    }

    // odds not included in the model, so assumption is the odds are always 1 and the bet amount is the whole stake
    private BigDecimal deltaAmount(Bet bet, boolean won) {
        val revokedWin = !won && WON == bet.result();
        return revokedWin ? bet.amount().multiply(new BigDecimal("-2")) : bet.amount();
    }
}
