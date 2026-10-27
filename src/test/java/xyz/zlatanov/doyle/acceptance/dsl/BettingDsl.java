package xyz.zlatanov.doyle.acceptance.dsl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.acceptance.driver.DoyleRestDriver;
import xyz.zlatanov.doyle.acceptance.driver.DoyleRestDriver.BetSettlementRow;
import xyz.zlatanov.doyle.acceptance.driver.DoyleRestDriver.EventData;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
public class BettingDsl {
    private final        TestContext     ctx;
    private final        DoyleRestDriver driver;
    private static final Duration        SETTLEMENT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration        POLL_INTERVAL      = Duration.ofMillis(250);

    public static Bets bet() {
        return bets(1);
    }

    public static Bets bets(int count) {
        return new Bets(count);
    }

    public static Settlement betsOn(String competitor) {
        return new Settlement(competitor);
    }

    public void place(Bets bets) {
        val betIds = driver.seedBets(UUID.randomUUID(), ctx.eventId(), ctx.eventMarketId(), ctx.competitorId(bets.competitor),
                bets.stake, bets.count);
        ctx.competitorBetIds(bets.competitor).addAll(betIds);
    }

    public void assertSettled(Settlement expected) {
        val betIds = ctx.competitorBetIds(expected.competitor);
        if (betIds == null) {
            throw new IllegalArgumentException("No bets placed on " + expected.competitor);
        }
        await().atMost(SETTLEMENT_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> assertSettled(expected, betIds, driver.getEventData(ctx.eventId())));
    }

    private void assertSettled(Settlement expected, List<UUID> betIds, EventData eventData) {
        for (val betId : betIds) {
            val bet = eventData.bets().stream()
                    .filter(b -> b.id().equals(betId))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Bet not found"));
            assertEquals("SETTLED", bet.status());
            assertEquals(expected.result, bet.result());
            if (expected.payouts != null) {
                assertThat(payouts(betId, eventData))
                        .usingElementComparator(BigDecimal::compareTo)
                        .containsExactlyElementsOf(expected.payouts);
            }
        }
    }

    private List<BigDecimal> payouts(UUID betId, EventData eventData) {
        return eventData.betSettlements().stream()
                .filter(s -> s.betId().equals(betId))
                .sorted(comparingInt(BetSettlementRow::settledEventVersion))
                .map(BetSettlementRow::deltaAmount)
                .toList();
    }


    public static class Bets {

        private final int        count;
        private       String     competitor;
        private       BigDecimal stake;

        private Bets(int count) {
            this.count = count;
        }

        public Bets on(String competitor) {
            this.competitor = competitor;
            return this;
        }

        public Bets stake(String amount) {
            this.stake = new BigDecimal(amount);
            return this;
        }
    }

    public static class Settlement {

        private final String           competitor;
        private       String           result;
        private       List<BigDecimal> payouts; // null when not checked

        private Settlement(String competitor) {
            this.competitor = competitor;
        }

        public Settlement won() {
            this.result = "WON";
            return this;
        }

        public Settlement lost() {
            this.result = "LOST";
            return this;
        }

        // payouts per bet, a negative one revokes an earlier payout
        public Settlement withPayouts(String... amounts) {
            this.payouts = Arrays.stream(amounts).map(BigDecimal::new).toList();
            return this;
        }

        public Settlement withNoPayouts() {
            this.payouts = List.of();
            return this;
        }
    }
}
