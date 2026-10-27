package xyz.zlatanov.doyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.zlatanov.doyle.core.*;
import xyz.zlatanov.doyle.core.port.driven.*;
import xyz.zlatanov.doyle.core.port.driving.*;

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

    @Bean
    SeedBetsUseCase seedBetsUseCase(BetRepository betRepository) {
        return new SeedBetsCommandProcessor(betRepository);
    }

    @Bean
    GetEventDataUseCase getEventDataService(EventDataRepository eventDataRepository) {
        return new GetEventDataService(eventDataRepository);
    }
}
