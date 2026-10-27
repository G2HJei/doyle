package xyz.zlatanov.doyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.zlatanov.doyle.adapter.driving.MatchEventOutcomeToBetsToSettleScheduledJob;
import xyz.zlatanov.doyle.adapter.driving.RelayEventOutcomesScheduledJob;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;
import xyz.zlatanov.doyle.core.port.driving.RelayEventOutcomesUseCase;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    RelayEventOutcomesScheduledJob relayEventOutcomesScheduledJob(RelayEventOutcomesUseCase relayEventOutcomesUseCase) {
        return new RelayEventOutcomesScheduledJob(relayEventOutcomesUseCase);
    }

    @Bean
    MatchEventOutcomeToBetsToSettleScheduledJob matchEventOutcomeToBetsToSettleScheduledJob(
            MatchEventOutcomeToBetsToSettleUseCase matchEventOutcomeToBetsToSettleUseCase) {
        return new MatchEventOutcomeToBetsToSettleScheduledJob(matchEventOutcomeToBetsToSettleUseCase);
    }
}
