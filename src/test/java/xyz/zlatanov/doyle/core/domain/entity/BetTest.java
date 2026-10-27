package xyz.zlatanov.doyle.core.domain.entity;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.entity.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.entity.BetSettlement.PayoutStatus.PENDING;

class BetTest {

    BigDecimal TWO = BigDecimal.valueOf(2);

    EventOutcome outcome = EventOutcome.restore()
            .eventId(new EventId(randomUUID()))
            .eventWinnerId(new CompetitorId(randomUUID()))
            .version(1)
            .build();

    @Test
    void shouldSettleBetAsLost() {
        val losingBet = bet(new CompetitorId(randomUUID()), PLACED, null, null); //not the winner
        val payout = losingBet.settle(outcome);
        assertResult(expectedBet(losingBet, LOST), null, losingBet, payout);
    }

    @Test
    void shouldSettleWinningBet() {
        val winningBet = bet(outcome.eventWinnerId(), PLACED, null, null);
        val payout = winningBet.settle(outcome);
        assertResult(
                expectedBet(winningBet, WON),
                expectedSettlement(winningBet, TWO),
                winningBet, payout);
    }

    @Test
    void shouldNoOpAlreadySettledBet() {
        val settledWinningBet = bet(outcome.eventWinnerId(), SETTLED, WON, 1);
        val payout = settledWinningBet.settle(outcome);
        assertResult(expectedBet(settledWinningBet, WON), null, settledWinningBet, payout);
    }

    @Test
    void shouldNoOpOnStaleOutcome() {
        val settledByNewerOutcomeBet = bet(new CompetitorId(randomUUID()), SETTLED, WON, 2);
        val payout = settledByNewerOutcomeBet.settle(outcome);
        assertResult(expectedBet(settledByNewerOutcomeBet, WON, 2), null, settledByNewerOutcomeBet, payout);
    }

    @Test
    void shouldResettleOnWinnerChanged() {
        val correctionBet = bet(new CompetitorId(randomUUID()), SETTLED, WON, 0);
        val payout = correctionBet.settle(outcome);
        assertResult(
                expectedBet(correctionBet, LOST),
                expectedSettlement(correctionBet, TWO.negate()),
                correctionBet, payout);
    }

    @Test
    void shouldSettleOnWinnerChanged() {
        val correctionLosingBet = bet(outcome.eventWinnerId(), SETTLED, LOST, 0);
        val payout = correctionLosingBet.settle(outcome);
        assertResult(
                expectedBet(correctionLosingBet, WON),
                expectedSettlement(correctionLosingBet, TWO),
                correctionLosingBet, payout);
    }

    @Test
    void shouldNotPayOutAgainOnUnchangedWinner() {
        val outdatedWinningBet = bet(outcome.eventWinnerId(), SETTLED, WON, 0);
        val payout = outdatedWinningBet.settle(outcome);
        assertResult(expectedBet(outdatedWinningBet, WON), null, outdatedWinningBet, payout);
    }

    @Test
    void shouldNotPayOutOnUnchangedLoss() {
        val outdatedLosingBet = bet(new CompetitorId(randomUUID()), SETTLED, LOST, 0);
        val payout = outdatedLosingBet.settle(outcome);
        assertResult(expectedBet(outdatedLosingBet, LOST), null, outdatedLosingBet, payout);
    }

    Bet bet(CompetitorId eventWinnerId, Bet.BetStatus status, Bet.BetResult result, Integer settledEventVersion) {
        return Bet.restore()
                .id(new BetId(randomUUID()))
                .eventId(outcome.eventId())
                .eventWinnerId(eventWinnerId)
                .amount(ONE)
                .status(status)
                .result(result)
                .settledEventVersion(settledEventVersion)
                .build();
    }

    Bet expectedBet(Bet bet, Bet.BetResult result) {
        return expectedBet(bet, result, 1);
    }

    Bet expectedBet(Bet bet, Bet.BetResult result, int settledEventVersion) {
        return bet.toBuilder()
                .status(SETTLED)
                .result(result)
                .settledEventVersion(settledEventVersion)
                .build();
    }

    BetSettlement expectedSettlement(Bet bet, BigDecimal amount) {
        return BetSettlement.restore()
                .betId(bet.id())
                .settledEventVersion(1)
                .deltaAmount(amount)
                .payoutStatus(PENDING)
                .build();
    }

    void assertResult(Bet expectedBet, BetSettlement expectedSettlement, Bet actualBet, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<BetSettlement> actualPayout) {
        assertEquals(expectedBet, actualBet);
        assertEquals(Optional.ofNullable(expectedSettlement), actualPayout);
    }
}
