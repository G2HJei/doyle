package xyz.zlatanov.doyle.acceptance;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import xyz.zlatanov.doyle.acceptance.driver.DoyleRestDriver;
import xyz.zlatanov.doyle.acceptance.dsl.BettingDsl;
import xyz.zlatanov.doyle.acceptance.dsl.EventDsl;
import xyz.zlatanov.doyle.acceptance.dsl.TestContext;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static xyz.zlatanov.doyle.acceptance.dsl.BettingDsl.*;

/**
 * Acceptance tests running against Doyle's public REST API.
 * <br/>
 * Doyle runs in-process against Kafka+RocketMQ of compose.yaml ({@code docker compose up -d}), as RocketMQ has no Testcontainers module.
 * <br/>
 * Excluded from {@code mvn test}; run manually with {@code mvn test -Dtest=BetSettlementAcceptanceTest} or directly in IntelliJ.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class BetSettlementAcceptanceTest {

    @LocalServerPort
    int port;

    BettingDsl betting;
    EventDsl   event;

    @BeforeEach
    void setUp() {
        val ctx = new TestContext();
        val driver = new DoyleRestDriver("http://localhost:" + port);
        betting = new BettingDsl(ctx, driver);
        event = new EventDsl(ctx, driver);
    }

    @Test
    void shouldSettleWinningAndLosingBetsWhenWinnerIsBroadcast() {
        betting.place(bets(2).on("Real Madrid CF").stake("10.00"));
        betting.place(bets(3).on("FC Barcelona").stake("5.00"));

        event.broadcastWinner("Real Madrid CF");

        betting.assertSettled(betsOn("Real Madrid CF").won().withPayouts("10.00"));
        betting.assertSettled(betsOn("FC Barcelona").lost().withNoPayouts());
    }

    @Test
    void shouldResettleBetsWhenWinnerIsCorrected() {
        betting.place(bet().on("Argentina").stake("10.00"));
        betting.place(bet().on("England").stake("20.00"));
        event.broadcastWinner("Argentina");
        betting.assertSettled(betsOn("Argentina").won().withPayouts("10.00"));
        betting.assertSettled(betsOn("England").lost().withNoPayouts());

        event.correctWinner("England");

        betting.assertSettled(betsOn("Argentina").lost().withPayouts("10.00", "-10.00"));
        betting.assertSettled(betsOn("England").won().withPayouts("20.00"));
    }
}
