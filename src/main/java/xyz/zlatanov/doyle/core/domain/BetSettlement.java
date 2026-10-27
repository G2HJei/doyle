package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class BetSettlement {

    private UUID         id;
    private UUID         betId;
    private BigDecimal   deltaAmount;
    private PayoutStatus payoutStatus;
    private int          settledEventVersion;

    public enum PayoutStatus {
        PENDING, PAID_OUT, FAILED
    }
}
