package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(fluent = true)
public class SeedBetsCommand {

    private BetTemplate template;
    private int         createCount;

    @Data
    @Accessors(fluent = true)
    public static class BetTemplate {

        private UserId        userId;
        private EventId       eventId;
        private EventMarketId eventMarketId;
        private CompetitorId  eventWinnerId;
        private BigDecimal    amount;
    }
}
