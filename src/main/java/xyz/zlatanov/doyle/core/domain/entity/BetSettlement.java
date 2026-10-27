package xyz.zlatanov.doyle.core.domain.entity;

import lombok.*;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.BetSettlementId;

import java.math.BigDecimal;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.doyle.core.domain.entity.BetSettlement.PayoutStatus.PENDING;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@Builder(builderMethodName = "restore", toBuilder = true) // restores stored settlements; new ones are created
@AllArgsConstructor(access = PRIVATE)
public class BetSettlement {

    private BetSettlementId id;
    private BetId           betId;
    private BigDecimal      deltaAmount;
    private PayoutStatus    payoutStatus;
    private int             settledEventVersion;

    public static BetSettlement create(BetId betId, BigDecimal deltaAmount, int settledEventVersion) {
        return new BetSettlementBuilder()
                .betId(betId)
                .deltaAmount(deltaAmount)
                .payoutStatus(PENDING)
                .settledEventVersion(settledEventVersion)
                .build();
    }

    public void assignId(BetSettlementId id) {
        assert this.id == null : "Bet settlement already has an id";
        this.id = id;
    }

    public enum PayoutStatus {
        PENDING, PAID_OUT, FAILED
    }
}
