package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.BetSettler.ToSave;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

@Slf4j
@RequiredArgsConstructor
public class SettleBetCommandProcessor implements SettleBetUseCase {

    private final EventOutcomeRepository  eventOutcomeRepository;
    private final BetRepository           betRepository;
    private final BetSettlementRepository betSettlementRepository;

    @Transactional
    @Override
    //todo SettleBetCommand(s) can be batched to reduce DB queries with one-by-one fallback in case of an error
    public void settle(SettleBetCommand command) {
        val bet = betRepository.findById(command.betId());
        val eventOutcome = eventOutcomeRepository.findOneByEventId(bet.eventId()); // todo can be cached
        val toSave = new BetSettler(bet, eventOutcome).settle();
        if (toSave.bet() != null) {
            betRepository.save(toSave.bet());
        }
        if (toSave.betSettlement() != null) {
            betSettlementRepository.save(toSave.betSettlement());
        }
        logSettlement(bet, eventOutcome, toSave);
    }

    private void logSettlement(Bet bet, EventOutcome eventOutcome, ToSave toSave) {
        if (toSave.bet() == null) {
            log.info("Bet already settled: betId={}, eventId={}, outcomeVersion={}",
                    bet.id(), bet.eventId(), eventOutcome.version());
        } else {
            log.info("Bet settled: betId={}, eventId={}, outcomeVersion={}, result={}, payout={}",
                    bet.id(), bet.eventId(), eventOutcome.version(), toSave.bet().result(),
                    toSave.betSettlement() == null ? "none" : toSave.betSettlement().deltaAmount());
        }
    }
}
