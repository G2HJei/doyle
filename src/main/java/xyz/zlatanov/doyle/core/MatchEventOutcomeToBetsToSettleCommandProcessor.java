package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

import java.util.List;

@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleCommandProcessor implements MatchEventOutcomeToBetsToSettleUseCase {

    private static final int CHUNK_SIZE = 100;

    private final SettlementJobRepository settlementJobRepository;
    private final BetRepository           betRepository;
    private final BetSettlementPublisher  betSettlementPublisher;

    @Override
    public void match() {
        val job = settlementJobRepository.nextPending();
        if (job == null) {
            return;
        }
        val bets = getBetsToSettle(job);
        settle(bets);
        settlementJobRepository.markCompleted(job.id());
    }

    private List<Bet> getBetsToSettle(SettlementJob job) {
        return betRepository.findByEventIdAndAfterIdAndLimit(job.eventId(), job.lastBetId(), CHUNK_SIZE);
    }

    private void settle(List<Bet> bets) {
        bets.forEach(b -> betSettlementPublisher.publish(new BetSettlementMessage().betId(b.id())));
    }
}
