package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.BetSettlement;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

import java.util.Objects;

import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.WON;
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
        if (!Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId())) {
            betRepository.updateStatusAndResult(betId, SETTLED, LOST);
        } else {
            betRepository.updateStatusAndResult(betId, SETTLED, WON);
            betSettlementRepository.save(new BetSettlement()
                    .betId(bet.id())
                    .settledEventVersion(eventOutcome.version())
                    .deltaAmount(bet.amount())
                    .payoutStatus(PENDING)); //odds not included in the model, so assumption is the bet amount is the whole stake
        }
    }
}
