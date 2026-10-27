package xyz.zlatanov.doyle.acceptance.dsl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.acceptance.driver.DoyleRestDriver;

@RequiredArgsConstructor
public class EventDsl {

    private final TestContext     ctx;
    private final DoyleRestDriver driver;

    public void broadcastWinner(String competitor) {
        val eventName = String.join(" - ", ctx.competitorIds().keySet());
        driver.publishEventOutcome(ctx.eventId(), eventName, ctx.competitorId(competitor));
    }

    public void correctWinner(String competitor) {
        broadcastWinner(competitor);
    }
}
