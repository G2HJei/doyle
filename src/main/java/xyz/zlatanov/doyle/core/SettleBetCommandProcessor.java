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
        val betId = command.betId();
        val bet = betRepository.findById(betId);
        val eventOutcome = eventOutcomeLookup.getEventOutcome(bet.eventId());
        if (bet.settledEventVersion() >= eventOutcome.version()) {
            return; // no op
        }
        val won = Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId());
        if (!won && PLACED == bet.status()) {
            betRepository.updateStatusAndResult(betId, SETTLED, LOST);
        } else {
            val settlement = getBetSettlement(bet, eventOutcome);
            betRepository.updateStatusAndResult(betId, SETTLED, won ? WON : LOST);
            betSettlementRepository.save(settlement);
        }
    }

    //odds not included in the model, so assumption is the odds are always 1
    private BetSettlement getBetSettlement(Bet bet, EventOutcome eventOutcome) {
        val won = Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId());
        if (!won && WON == bet.result()) {
            return new BetSettlement()
                    .betId(bet.id())
                    .settledEventVersion(eventOutcome.version())
                    .deltaAmount(bet.amount().multiply(new BigDecimal("-2")))
                    .payoutStatus(PENDING);
        }
        return new BetSettlement()
                .betId(bet.id())
                .settledEventVersion(eventOutcome.version())
                .deltaAmount(bet.amount()) //odds not included in the model, so assumption is the bet amount is the whole stake
                .payoutStatus(PENDING);
    }
}
