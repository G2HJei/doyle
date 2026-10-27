package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import xyz.zlatanov.doyle.core.port.driving.RelayEventOutcomesUseCase;

@RequiredArgsConstructor
public class RelayEventOutcomesScheduledJob {

    private final RelayEventOutcomesUseCase relayEventOutcomesUseCase;

    @Scheduled(fixedDelayString = "${doyle.outcome-relay.fixed-delay}")
    public void run() {
        relayEventOutcomesUseCase.relayOutcomes();
    }
}
