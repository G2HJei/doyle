package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

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
        val bets = betRepository.findByEventIdAndAfterIdAndLimit(job.eventId(), job.lastBetId(), CHUNK_SIZE);
        bets.forEach(b -> betSettlementPublisher.publish(new BetSettlementMessage().betId(b.id())));
        settlementJobRepository.markCompleted(job.id());
    }
}
