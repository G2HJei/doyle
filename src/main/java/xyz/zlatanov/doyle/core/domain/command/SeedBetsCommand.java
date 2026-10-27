package xyz.zlatanov.doyle.core.domain.command;

import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventMarketId;
import xyz.zlatanov.doyle.core.domain.id.UserId;

import java.math.BigDecimal;

public record SeedBetsCommand(BetTemplate template, int createCount) {

    public record BetTemplate(UserId userId,
                              EventId eventId,
                              EventMarketId eventMarketId,
                              CompetitorId eventWinnerId,
                              BigDecimal amount) {
    }
}
