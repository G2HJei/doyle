package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(fluent = true)
public class Bet {

    private BetId         id;
    private UserId        userId;
    private EventId       eventId;
    private EventMarketId eventMarketId;
    private CompetitorId  eventWinnerId;
    private BigDecimal    amount;
    private BetStatus     status;
    private BetResult     result;
    private Integer       settledEventVersion;
    private int           version;

    public enum BetStatus {
        PLACED, SETTLED
    }

    public enum BetResult {
        WON, LOST
    }

}
