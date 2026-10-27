package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.BetSettler;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

@RequiredArgsConstructor
public class SettleBetCommandProcessor implements SettleBetUseCase {

    private final EventOutcomeLookup      eventOutcomeLookup;
    private final BetRepository           betRepository;
    private final BetSettlementRepository betSettlementRepository;

    @Override
    public void settle(SettleBetCommand command) {
        val bet = betRepository.findById(command.betId());
        val eventOutcome = eventOutcomeLookup.getEventOutcome(bet.eventId());
        val toSave = new BetSettler(bet, eventOutcome).settle();
        if (toSave.bet() != null) {
            betRepository.save(toSave.bet());
        }
        if (toSave.betSettlement() != null) {
            betSettlementRepository.save(toSave.betSettlement());
        }
    }
}
