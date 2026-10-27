package xyz.zlatanov.doyle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.adapter.driven.JdbcBetRepository;
import xyz.zlatanov.doyle.adapter.driven.JdbcBetSettlementRepository;
import xyz.zlatanov.doyle.adapter.driven.JdbcEventOutcomeRepository;
import xyz.zlatanov.doyle.adapter.driven.JdbcSettlementJobRepository;
import xyz.zlatanov.doyle.adapter.driven.UuidV7Service;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

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
    SettlementJobRepository settlementJobRepository(JdbcClient jdbcClient, UUIDService uuidService) {
        return new JdbcSettlementJobRepository(jdbcClient, uuidService);
    }
}
