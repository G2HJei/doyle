package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleCommandProcessor implements MatchEventOutcomeToBetsToSettleUseCase {

    private final SettlementJobRepository settlementJobRepository;
    private final BetSettlementPublisher  betSettlementPublisher;

    @Override
    public void match() {
        val job = settlementJobRepository.nextPending();
        settlementJobRepository.markCompleted(job.id());
    }
}
