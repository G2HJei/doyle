package xyz.zlatanov.doyle.core.domain;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.LOST;
import static xyz.zlatanov.doyle.core.domain.Bet.BetResult.WON;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.SETTLED;
import static xyz.zlatanov.doyle.core.domain.BetSettlement.PayoutStatus.PENDING;

class BetSettlerTest {

    EventOutcome outcome = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventWinnerId(UUID.randomUUID())
            .version(1);

    @Test
    void shouldSettleBetAsLost() {
        val losingBet = bet(UUID.randomUUID(), PLACED, null, null); //not the winner

        val toSave = new BetSettler(losingBet, outcome).settle();

        assertEquals(expected(losingBet, LOST), toSave.bet());
        assertNull(toSave.betSettlement());
    }

    @Test
    void shouldSettleWinningBet() {
        val winningBet = bet(outcome.eventWinnerId(), PLACED, null, null);

        val toSave = new BetSettler(winningBet, outcome).settle();

        assertEquals(expected(winningBet, WON), toSave.bet());
        assertEquals(new BetSettlement()
                .betId(winningBet.id())
                .settledEventVersion(1)
                .deltaAmount(ONE)
                .payoutStatus(PENDING), toSave.betSettlement());
    }

    @Test
    void shouldNoOpAlreadySettledBet() {
        val settledWinningBet = bet(outcome.eventWinnerId(), SETTLED, WON, 1);

        val toSave = new BetSettler(settledWinningBet, outcome).settle();

        assertNull(toSave.bet());
        assertNull(toSave.betSettlement());
    }

    @Test
    void shouldResettleOnWinnerChanged() {
        val correctionBet = bet(UUID.randomUUID(), SETTLED, WON, 0);

        val toSave = new BetSettler(correctionBet, outcome).settle();

        assertEquals(expected(correctionBet, LOST), toSave.bet());
        assertEquals(new BetSettlement()
                .betId(correctionBet.id())
                .settledEventVersion(1)
                .deltaAmount(new BigDecimal("-1"))
                .payoutStatus(PENDING), toSave.betSettlement());
    }

    @Test
    void shouldSettleOnWinnerChanged() {
        val correctionLosingBet = bet(outcome.eventWinnerId(), SETTLED, LOST, 0);

        val toSave = new BetSettler(correctionLosingBet, outcome).settle();

        assertEquals(expected(correctionLosingBet, WON), toSave.bet());
        assertEquals(new BetSettlement()
                .betId(correctionLosingBet.id())
                .settledEventVersion(1)
                .deltaAmount(ONE)
                .payoutStatus(PENDING), toSave.betSettlement());
    }

    @Test
    void shouldNotPayOutAgainOnUnchangedWinner() {
        val outdatedWinningBet = bet(outcome.eventWinnerId(), SETTLED, WON, 0);

        val toSave = new BetSettler(outdatedWinningBet, outcome).settle();

        assertEquals(expected(outdatedWinningBet, WON), toSave.bet());
        assertNull(toSave.betSettlement());
    }

    @Test
    void shouldNotPayOutOnUnchangedLoss() {
        val outdatedLosingBet = bet(UUID.randomUUID(), SETTLED, LOST, 0);

        val toSave = new BetSettler(outdatedLosingBet, outcome).settle();

        assertEquals(expected(outdatedLosingBet, LOST), toSave.bet());
        assertNull(toSave.betSettlement());
    }

    Bet bet(UUID eventWinnerId, Bet.BetStatus status, Bet.BetResult result, Integer settledEventVersion) {
        return new Bet()
                .id(UUID.randomUUID())
                .eventId(outcome.eventId())
                .eventWinnerId(eventWinnerId)
                .amount(ONE)
                .status(status)
                .result(result)
                .settledEventVersion(settledEventVersion);
    }

    Bet expected(Bet bet, Bet.BetResult result) {
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
}
