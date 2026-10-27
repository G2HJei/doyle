package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

import java.time.Duration;

@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleScheduledJob {

    private final static Duration MAX_RUN_TIME = Duration.ofSeconds(1);

    private final MatchEventOutcomeToBetsToSettleUseCase useCase;

    @Scheduled(fixedDelay = 1000)
    public void run() {
        val start = System.nanoTime();
        boolean moreToMatch;
        do {
            moreToMatch = useCase.match(); // a chunk per call, each in its own transaction
        } while (moreToMatch && System.nanoTime() - start < MAX_RUN_TIME.toNanos());
    }
}
