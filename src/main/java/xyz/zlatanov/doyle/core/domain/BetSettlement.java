package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(fluent = true)
public class BetSettlement {

    private BetSettlementId id;
    private BetId           betId;
    private BigDecimal      deltaAmount;
    private PayoutStatus    payoutStatus;
    private int             settledEventVersion;

    public enum PayoutStatus {
        PENDING, PAID_OUT, FAILED
    }
}
