package xyz.zlatanov.doyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.zlatanov.doyle.core.ConsumeEventOutcomeService;
import xyz.zlatanov.doyle.core.MatchEventOutcomeToBetsToSettleCommandProcessor;
import xyz.zlatanov.doyle.core.PublishEventOutcomeCommandProcessor;
import xyz.zlatanov.doyle.core.RelayEventOutcomesCommandProcessor;
import xyz.zlatanov.doyle.core.SettleBetCommandProcessor;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;
import xyz.zlatanov.doyle.core.port.driving.RelayEventOutcomesUseCase;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

@Configuration
public class CoreConfig {

    @Bean
    PublishEventOutcomeUseCase publishEventOutcomeUseCase(EventOutcomeRepository eventOutcomeRepository) {
        return new PublishEventOutcomeCommandProcessor(eventOutcomeRepository);
    }

    @Bean
    RelayEventOutcomesUseCase relayEventOutcomesUseCase(EventOutcomeRepository eventOutcomeRepository,
                                                        EventOutcomePublisher eventOutcomePublisher) {
        return new RelayEventOutcomesCommandProcessor(eventOutcomeRepository, eventOutcomePublisher);
    }

    @Bean
    ConsumeEventOutcomeUseCase consumeEventOutcomeUseCase(SettlementJobRepository settlementJobRepository) {
        return new ConsumeEventOutcomeService(settlementJobRepository);
    }

    @Bean
    MatchEventOutcomeToBetsToSettleUseCase matchEventOutcomeToBetsToSettleUseCase(
            SettlementJobRepository settlementJobRepository,
            BetRepository betRepository,
            BetSettlementPublisher betSettlementPublisher) {
        return new MatchEventOutcomeToBetsToSettleCommandProcessor(settlementJobRepository, betRepository,
                betSettlementPublisher);
    }

    @Bean
    SettleBetUseCase settleBetUseCase(EventOutcomeRepository eventOutcomeRepository,
                                      BetRepository betRepository,
                                      BetSettlementRepository betSettlementRepository) {
        return new SettleBetCommandProcessor(eventOutcomeRepository, betRepository, betSettlementRepository);
    }
}
