package xyz.zlatanov.doyle.core.domain;

import xyz.zlatanov.doyle.core.domain.Bet.BetResult;

import java.math.BigDecimal;
import java.util.Objects;

import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.BetSettlement.PayoutStatus.PENDING;

public class BetSettler {

    private final Bet          bet;
    private final EventOutcome eventOutcome;
    private final boolean      firstSettle;
    private final BetResult    oldResult;
    private final BetResult    newResult;

    public BetSettler(Bet bet, EventOutcome eventOutcome) {
        this.bet = bet;
        this.eventOutcome = eventOutcome;
        this.firstSettle = PLACED == bet.status();
        this.oldResult = bet.result();
        this.newResult = Objects.equals(bet.eventWinnerId(), eventOutcome.eventWinnerId()) ? WON : LOST;
    }

    public ToSave settle() {
        if (outcomeAlreadySettled()) {
            return ToSave.NOTHING;
        }
        return new ToSave(settledBet(), payout());
    }

    private boolean outcomeAlreadySettled() {
        return bet.settledEventVersion() >= eventOutcome.version();
    }

    private Bet settledBet() {
        return bet
                .status(SETTLED)
                .result(newResult)
                .settledEventVersion(eventOutcome.version());
    }

    private BetSettlement payout() {
        if (!resultChanged() || lostOnFirstSettle()) {
            return null; // nothing to pay out
        }
        return new BetSettlement()
                .betId(bet.id())
                .settledEventVersion(eventOutcome.version())
                .deltaAmount(deltaAmount())
                .payoutStatus(PENDING);
    }

    private boolean resultChanged() {
        return newResult != oldResult;
    }

    private boolean lostOnFirstSettle() {
        return firstSettle && LOST == newResult;
    }

    // odds not included in the model, so assumption is the odds are always 2 and the bet amount is equal to the payout
    private BigDecimal deltaAmount() {
        return revokedWin() ? bet.amount().negate() : bet.amount();
    }

    private boolean revokedWin() {
        return WON == oldResult && LOST == newResult;
    }

    public record ToSave(Bet bet, BetSettlement betSettlement) {

        public static final ToSave NOTHING = new ToSave(null, null);
    }
}
