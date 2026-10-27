package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

@RequiredArgsConstructor
public class SettleBetCommandProcessor implements SettleBetUseCase {

    private final EventOutcomeRepository  eventOutcomeRepository;
    private final BetRepository           betRepository;
    private final BetSettlementRepository betSettlementRepository;

    @Transactional
    @Override
    //todo SettleBetCommand(s) can be batched to reduce to 4 DB queries with one-by-one fallback in case of an error
    public void settle(SettleBetCommand command) {
        val bet = betRepository.findById(command.betId());
        val eventOutcome = eventOutcomeRepository.findOneByEventId(bet.eventId());
        val toSave = new BetSettler(bet, eventOutcome).settle();
        //todo extract transactional outbox to reduce transaction time
        if (toSave.bet() != null) {
            betRepository.save(toSave.bet());
        }
        if (toSave.betSettlement() != null) {
            betSettlementRepository.save(toSave.betSettlement());
        }
    }
}
