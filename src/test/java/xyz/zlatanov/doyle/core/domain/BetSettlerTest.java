package xyz.zlatanov.doyle.core.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.doyle.core.BetSettler;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.BetSettlement.PayoutStatus.PENDING;

class BetSettlerTest {

    EventOutcome outcome = new EventOutcome()
            .eventId(new EventId(randomUUID()))
            .eventWinnerId(new CompetitorId(randomUUID()))
            .version(1);

    @Test
    void shouldSettleBetAsLost() {
        val losingBet = bet(new CompetitorId(randomUUID()), PLACED, null, null); //not the winner
        val toSave = new BetSettler(losingBet, outcome).settle();
        assertResult(expectedBet(losingBet, LOST), null, toSave);
    }

    @Test
    void shouldSettleWinningBet() {
        val winningBet = bet(outcome.eventWinnerId(), PLACED, null, null);
        val toSave = new BetSettler(winningBet, outcome).settle();
        assertResult(
                expectedBet(winningBet, WON),
                expectedSettlement(winningBet, ONE),
                toSave);
    }

    @Test
    void shouldNoOpAlreadySettledBet() {
        val settledWinningBet = bet(outcome.eventWinnerId(), SETTLED, WON, 1);
        val toSave = new BetSettler(settledWinningBet, outcome).settle();
        assertResult(null, null, toSave);
    }

    @Test
    void shouldResettleOnWinnerChanged() {
        val correctionBet = bet(new CompetitorId(randomUUID()), SETTLED, WON, 0);
        val toSave = new BetSettler(correctionBet, outcome).settle();
        assertResult(
                expectedBet(correctionBet, LOST),
                expectedSettlement(correctionBet, ONE.negate()),
                toSave);
    }

    @Test
    void shouldSettleOnWinnerChanged() {
        val correctionLosingBet = bet(outcome.eventWinnerId(), SETTLED, LOST, 0);
        val toSave = new BetSettler(correctionLosingBet, outcome).settle();
        assertResult(
                expectedBet(correctionLosingBet, WON),
                expectedSettlement(correctionLosingBet, ONE),
                toSave);
    }

    @Test
    void shouldNotPayOutAgainOnUnchangedWinner() {
        val outdatedWinningBet = bet(outcome.eventWinnerId(), SETTLED, WON, 0);
        val toSave = new BetSettler(outdatedWinningBet, outcome).settle();
        assertResult(expectedBet(outdatedWinningBet, WON), null, toSave);
    }

    @Test
    void shouldNotPayOutOnUnchangedLoss() {
        val outdatedLosingBet = bet(new CompetitorId(randomUUID()), SETTLED, LOST, 0);
        val toSave = new BetSettler(outdatedLosingBet, outcome).settle();
        assertResult(expectedBet(outdatedLosingBet, LOST), null, toSave);
    }

    Bet bet(CompetitorId eventWinnerId, Bet.BetStatus status, Bet.BetResult result, Integer settledEventVersion) {
        return new Bet()
                .id(new BetId(randomUUID()))
                .eventId(outcome.eventId())
                .eventWinnerId(eventWinnerId)
                .amount(ONE)
                .status(status)
                .result(result)
                .settledEventVersion(settledEventVersion);
    }

    Bet expectedBet(Bet bet, Bet.BetResult result) {
        return new Bet()
                .id(bet.id())
                .userId(bet.userId())
                .eventId(bet.eventId())
                .eventMarketId(bet.eventMarketId())
                .eventWinnerId(bet.eventWinnerId())
                .amount(bet.amount())
                .status(SETTLED)
                .result(result)
                .settledEventVersion(1);
    }

    BetSettlement expectedSettlement(Bet bet, BigDecimal amount) {
        return new BetSettlement()
                .betId(bet.id())
                .settledEventVersion(1)
                .deltaAmount(amount)
                .payoutStatus(PENDING);
    }

    void assertResult(Bet expectedBet, BetSettlement expectedSettlement, BetSettler.ToSave actual) {
        assertEquals(expectedBet, actual.bet());
        assertEquals(expectedSettlement, actual.betSettlement());
    }
}
