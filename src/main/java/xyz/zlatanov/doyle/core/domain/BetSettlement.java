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
    private int          settledEventVersion;
    private BigDecimal   deltaAmount;
    private PayoutStatus payoutStatus;

    public enum PayoutStatus {
        PENDING, PAID_OUT, FAILED
    }
}
