package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.command.SettleBetCommand;
import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.entity.BetSettlement;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

@Slf4j
@RequiredArgsConstructor
public class SettleBetCommandProcessor implements SettleBetUseCase {

    private final EventOutcomeLookup      eventOutcomeLookup;
    private final BetRepository           betRepository;
    private final BetSettlementRepository betSettlementRepository;

    @Transactional
    @Override
    //todo SettleBetCommand(s) can be batched to reduce DB queries with one-by-one fallback in case of an error
    public void settle(SettleBetCommand command) {
        val bet = getBet(command);
        val eventOutcome = getEventOutcome(bet);
        if (bet.alreadySettledFor(eventOutcome)) {
            log.info("Bet already settled: betId={}, eventId={}, outcomeVersion={}", bet.id(), bet.eventId(), eventOutcome.version());
            return;
        }
        val payout = bet.settle(eventOutcome);
        betRepository.save(bet);
        payout.ifPresent(betSettlementRepository::save);

        log.info("Bet settled: betId={}, eventId={}, outcomeVersion={}, result={}, payout={}",
                bet.id(), bet.eventId(), eventOutcome.version(), bet.result(), payout.map(BetSettlement::deltaAmount).map(Object::toString).orElse("none"));
    }

    private Bet getBet(SettleBetCommand command) {
        return betRepository.findById(command.betId())
                .orElseThrow(() -> new IllegalStateException("Bet not found:" + command.betId()));
    }

    private EventOutcome getEventOutcome(Bet bet) {
        return eventOutcomeLookup.findOneByEventId(bet.eventId())
                .orElseThrow(() -> new IllegalStateException("EventOutcome not found:" + bet.eventId()));
    }
}
