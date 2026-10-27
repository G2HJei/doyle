package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.entity.SettlementJob;
import xyz.zlatanov.doyle.core.domain.message.BetSettlementMessage;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleCommandProcessor implements MatchEventOutcomeToBetsToSettleUseCase {

    private final SettlementJobRepository settlementJobRepository;
    private final BetRepository           betRepository;
    private final BetSettlementPublisher  betSettlementPublisher;
    private final int                     chunkSize;

    @Transactional
    @Override
    public boolean match() {
        val job = settlementJobRepository.nextPending();
        job.ifPresent(this::match);
        return job.isPresent();
    }

    private void match(SettlementJob job) {
        val bets = getBetsToSettle(job);
        if (bets.isEmpty()) {
            completeJob(job);
            log.info("Settlement job completed: jobId={}, eventId={}, outcomeVersion={}", job.id(), job.eventId(), job.outcomeVersion());
        } else {
            publishBetSettlements(bets);
            updateJobProgress(job, bets);
            log.info("Bet settlements sent: jobId={}, eventId={}, outcomeVersion={}, count={}, lastBetId={}",
                    job.id(), job.eventId(), job.outcomeVersion(), bets.size(), job.lastBetId());
        }
    }

    private List<Bet> getBetsToSettle(SettlementJob job) {
        return betRepository.findByEventIdAndAfterIdAndLimit(job.eventId(), job.lastBetId(), chunkSize);
    }

    private void completeJob(SettlementJob job) {
        job.complete();
        settlementJobRepository.save(job);
    }

    private void publishBetSettlements(List<Bet> bets) {
        bets.stream()
                .map(BetSettlementMessage::new)
                .forEach(betSettlementPublisher::publish);
    }

    private void updateJobProgress(SettlementJob job, List<Bet> bets) {
        val lastBetId = bets.get(bets.size() - 1).id();
        job.recordProgress(lastBetId);
        settlementJobRepository.save(job);
    }
}
