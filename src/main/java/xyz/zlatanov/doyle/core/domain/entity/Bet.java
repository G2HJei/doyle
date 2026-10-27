package xyz.zlatanov.doyle.core.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.id.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.entity.BetSettlement.PayoutStatus.PENDING;

@Data
@Accessors(fluent = true)
public class Bet {

    // Odds aren't in the model; assume fixed decimal odds of 2.0 (even money):
    // - a win pays out twice the stake
    // - revoking a win (WON -> LOST) charges back that full payout
    private static final BigDecimal ODDS = BigDecimal.valueOf(2);

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

    public boolean alreadySettledFor(EventOutcome eventOutcome) {
        return settledEventVersion != null && settledEventVersion >= eventOutcome.version();
    }

    /**
     * No-op if the bet is already settled for this or a later outcome version.
     *
     * @return the payout due, if the settlement changes what the bet has paid out
     */
    public Optional<BetSettlement> settle(EventOutcome eventOutcome) {
        if (alreadySettledFor(eventOutcome)) {
            return Optional.empty();
        }
        val newResult = resultFor(eventOutcome);
        val payout = payout(newResult, eventOutcome); // before the state changes, as it depends on the previous result
        status = SETTLED;
        result = newResult;
        settledEventVersion = eventOutcome.version();
        return payout;
    }

    private BetResult resultFor(EventOutcome eventOutcome) {
        return Objects.equals(eventWinnerId, eventOutcome.eventWinnerId()) ? WON : LOST;
    }

    private Optional<BetSettlement> payout(BetResult newResult, EventOutcome eventOutcome) {
        if (!resultChanges(newResult) || lostOnFirstSettle(newResult)) {
            return Optional.empty(); // nothing to pay out
        }
        return Optional.of(new BetSettlement()
                .betId(id)
                .settledEventVersion(eventOutcome.version())
                .deltaAmount(deltaAmount(newResult))
                .payoutStatus(PENDING));
    }

    private boolean resultChanges(BetResult newResult) {
        return newResult != result;
    }

    private boolean lostOnFirstSettle(BetResult newResult) {
        return PLACED == status && LOST == newResult;
    }

    private BigDecimal deltaAmount(BetResult newResult) {
        val payoutAmount = amount.multiply(ODDS);
        return revokesWin(newResult) ? payoutAmount.negate() : payoutAmount;
    }

    private boolean revokesWin(BetResult newResult) {
        return WON == result && LOST == newResult;
    }

    public enum BetStatus {
        PLACED, SETTLED
    }

    public enum BetResult {
        WON, LOST
    }

}
