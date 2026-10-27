package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

import java.time.Duration;

@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleScheduledJob {

    private final MatchEventOutcomeToBetsToSettleUseCase useCase;
    private final Duration                               maxRunTime;

    @Scheduled(fixedDelayString = "${doyle.bet-matching.fixed-delay}")
    public void run() {
        val start = System.nanoTime();
        boolean moreToMatch;
        do {
            moreToMatch = useCase.match(); // a chunk per call, each in its own transaction
        } while (moreToMatch && System.nanoTime() - start < maxRunTime.toNanos());
    }
}
