package xyz.zlatanov.doyle.core.domain.entity;

import lombok.*;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetStatus.SETTLED;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@Builder(builderMethodName = "restore", toBuilder = true) // restores stored bets; new ones are placed
@AllArgsConstructor(access = PRIVATE)
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

    public static Bet place(UserId userId, EventId eventId, EventMarketId eventMarketId, CompetitorId eventWinnerId,
                            BigDecimal amount) {
        return new BetBuilder()
                .userId(userId)
                .eventId(eventId)
                .eventMarketId(eventMarketId)
                .eventWinnerId(eventWinnerId)
                .amount(amount)
                .status(PLACED)
                .build();
    }

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

    public void assignId(BetId id) {
        assert this.id == null : "Bet already has an id";
        this.id = id;
    }

    public void incrementVersion() {
        version++;
    }

    private BetResult resultFor(EventOutcome eventOutcome) {
        return Objects.equals(eventWinnerId, eventOutcome.eventWinnerId()) ? WON : LOST;
    }

    private Optional<BetSettlement> payout(BetResult newResult, EventOutcome eventOutcome) {
        if (!resultChanges(newResult) || lostOnFirstSettle(newResult)) {
            return Optional.empty(); // nothing to pay out
        }
        return Optional.of(BetSettlement.create(id, deltaAmount(newResult), eventOutcome.version()));
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
