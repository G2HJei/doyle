package xyz.zlatanov.doyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.adapter.driven.*;
import xyz.zlatanov.doyle.core.port.driven.*;

@Configuration
public class PersistenceConfig {

    @Bean
    UUIDProvider uuidService() {
        return new UuidV7Provider();
    }

    @Bean
    BetRepository betRepository(JdbcClient jdbcClient, UUIDProvider uuidService) {
        return new JdbcBetRepository(jdbcClient, uuidService);
    }

    @Bean
    BetSettlementRepository betSettlementRepository(JdbcClient jdbcClient, UUIDProvider uuidService) {
        return new JdbcBetSettlementRepository(jdbcClient, uuidService);
    }

    @Bean
    EventOutcomeRepository eventOutcomeRepository(JdbcClient jdbcClient, UUIDProvider uuidService) {
        return new JdbcEventOutcomeRepository(jdbcClient, uuidService);
    }

    @Bean
    EventOutcomeLookup eventOutcomeLookup(EventOutcomeRepository eventOutcomeRepository) {
        return new CachingEventOutcomeLookup(eventOutcomeRepository);
    }

    @Bean
    SettlementJobRepository settlementJobRepository(JdbcClient jdbcClient, UUIDProvider uuidService) {
        return new JdbcSettlementJobRepository(jdbcClient, uuidService);
    }

    @Bean
    EventDataRepository eventDataRepository(JdbcClient jdbcClient) {
        return new JdbcEventDataRepository(jdbcClient);
    }
}
