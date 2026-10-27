package xyz.zlatanov.doyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.adapter.driven.*;
import xyz.zlatanov.doyle.core.port.driven.*;

@Configuration
public class PersistenceConfig {

    @Bean
    UUIDService uuidService() {
        return new UuidV7Service();
    }

    @Bean
    BetRepository betRepository(JdbcClient jdbcClient, UUIDService uuidService) {
        return new JdbcBetRepository(jdbcClient, uuidService);
    }

    @Bean
    BetSettlementRepository betSettlementRepository(JdbcClient jdbcClient, UUIDService uuidService) {
        return new JdbcBetSettlementRepository(jdbcClient, uuidService);
    }

    @Bean
    EventOutcomeRepository eventOutcomeRepository(JdbcClient jdbcClient, UUIDService uuidService) {
        return new JdbcEventOutcomeRepository(jdbcClient, uuidService);
    }

    @Bean
    EventOutcomeLookup eventOutcomeLookup(EventOutcomeRepository eventOutcomeRepository) {
        return new CachingEventOutcomeLookup(eventOutcomeRepository);
    }

    @Bean
    SettlementJobRepository settlementJobRepository(JdbcClient jdbcClient, UUIDService uuidService) {
        return new JdbcSettlementJobRepository(jdbcClient, uuidService);
    }

    @Bean
    EventDataRepository eventDataRepository(JdbcClient jdbcClient) {
        return new JdbcEventDataRepository(jdbcClient);
    }
}
