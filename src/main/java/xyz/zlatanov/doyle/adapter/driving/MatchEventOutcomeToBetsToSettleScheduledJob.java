package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

@RequiredArgsConstructor
public class MatchEventOutcomeToBetsToSettleScheduledJob {

    private final MatchEventOutcomeToBetsToSettleUseCase matchEventOutcomeToBetsToSettleUseCase;

    @Scheduled(fixedDelay = 1000)
    public void run() {
        matchEventOutcomeToBetsToSettleUseCase.match();
    }
}
