package xyz.zlatanov.doyle.adapter.driving;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import xyz.zlatanov.doyle.core.domain.command.SeedBetsCommand;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventMarketId;
import xyz.zlatanov.doyle.core.domain.id.UserId;

import java.math.BigDecimal;
import java.util.UUID;

public record SeedBetsRequest(@NotNull @Valid BetTemplate template,
                              @NotNull @Positive Integer createCount) {

    SeedBetsCommand toCommand() {
        return new SeedBetsCommand()
                .template(template.toTemplate())
                .createCount(createCount);
    }

    public record BetTemplate(@NotNull UUID userId,
                              @NotNull UUID eventId,
                              @NotNull UUID eventMarketId,
                              @NotNull UUID eventWinnerId,
                              @NotNull @Positive BigDecimal amount) {

        SeedBetsCommand.BetTemplate toTemplate() {
            return new SeedBetsCommand.BetTemplate()
                    .userId(new UserId(userId))
                    .eventId(new EventId(eventId))
                    .eventMarketId(new EventMarketId(eventMarketId))
                    .eventWinnerId(new CompetitorId(eventWinnerId))
                    .amount(amount);
        }
    }
}
