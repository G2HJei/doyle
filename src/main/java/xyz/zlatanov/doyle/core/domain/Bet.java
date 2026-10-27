package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class Bet {

    private UUID       id;
    private UUID       userId;
    private UUID       eventId;
    private UUID       eventMarketId;
    private UUID       eventWinnerId;
    private BigDecimal amount;
    private BetStatus  status;
    private BetResult  result;
    private int        settledEventVersion;

    public enum BetStatus {
        PLACED, SETTLED
    }

    public enum BetResult {
        WON, LOST
    }

}
