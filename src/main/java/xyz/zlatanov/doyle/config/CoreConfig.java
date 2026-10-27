package xyz.zlatanov.doyle.config;

import org.springframework.beans.factory.annotation.Value;
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
                                                        EventOutcomePublisher eventOutcomePublisher,
                                                        @Value("${doyle.outcome-relay.chunk-size}") int chunkSize) {
        return new RelayEventOutcomesCommandProcessor(eventOutcomeRepository, eventOutcomePublisher, chunkSize);
    }

    @Bean
    PropagateEventOutcomeUseCase consumeEventOutcomeUseCase(SettlementJobRepository settlementJobRepository) {
        return new PropagateEventOutcomeCommandProcessor(settlementJobRepository);
    }

    @Bean
    MatchEventOutcomeToBetsToSettleUseCase matchEventOutcomeToBetsToSettleUseCase(
            SettlementJobRepository settlementJobRepository,
            BetRepository betRepository,
            BetSettlementPublisher betSettlementPublisher,
            @Value("${doyle.bet-matching.chunk-size}") int chunkSize) {
        return new MatchEventOutcomeToBetsToSettleCommandProcessor(settlementJobRepository, betRepository,
                betSettlementPublisher, chunkSize);
    }

    @Bean
    SettleBetUseCase settleBetUseCase(EventOutcomeLookup eventOutcomeLookup,
                                      BetRepository betRepository,
                                      BetSettlementRepository betSettlementRepository) {
        return new SettleBetCommandProcessor(eventOutcomeLookup, betRepository, betSettlementRepository);
    }

    @Bean
    SeedBetsUseCase seedBetsUseCase(BetRepository betRepository) {
        return new SeedBetsCommandProcessor(betRepository);
    }

    @Bean
    GetEventDataUseCase getEventDataUseCase(EventDataRepository eventDataRepository) {
        return new GetEventDataQuery(eventDataRepository);
    }
}
