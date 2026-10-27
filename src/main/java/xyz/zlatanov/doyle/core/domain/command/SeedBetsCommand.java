package xyz.zlatanov.doyle.core.domain.command;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventMarketId;
import xyz.zlatanov.doyle.core.domain.id.UserId;

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
