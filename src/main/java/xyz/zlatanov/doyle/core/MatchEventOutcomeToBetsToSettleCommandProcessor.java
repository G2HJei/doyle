package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleCommandProcessor implements MatchEventOutcomeToBetsToSettleUseCase {

    static final int CHUNK_SIZE = 2;

    private final SettlementJobRepository settlementJobRepository;
    private final BetRepository           betRepository;
    private final BetSettlementPublisher  betSettlementPublisher;

    @Transactional
    @Override
    public void match() {
        val job = settlementJobRepository.nextPending();
        if (job == null) {
            return;
        }
        val bets = getBetsToSettle(job);
        if (bets.isEmpty()) {
            settlementJobRepository.save(job.completed(true));
            log.info("Settlement job completed: jobId={}, eventId={}, outcomeVersion={}", job.id(), job.eventId(), job.outcomeVersion());
        } else {
            settle(bets);
            settlementJobRepository.save(job.lastBetId(bets.get(bets.size() - 1).id()));
            log.info("Bet settlements sent: jobId={}, eventId={}, outcomeVersion={}, count={}, lastBetId={}",
                    job.id(), job.eventId(), job.outcomeVersion(), bets.size(), job.lastBetId());
        }
    }

    private List<Bet> getBetsToSettle(SettlementJob job) {
        return betRepository.findByEventIdAndAfterIdAndLimit(job.eventId(), job.lastBetId(), CHUNK_SIZE);
    }

    private void settle(List<Bet> bets) {
        bets.forEach(b -> betSettlementPublisher.publish(new BetSettlementMessage().betId(b.id())));
    }
}
