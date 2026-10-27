package xyz.zlatanov.doyle.adapter.driving;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import xyz.zlatanov.doyle.core.domain.CompetitorId;
import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;

import java.util.UUID;

public record PublishEventOutcomeRequest(@NotNull UUID eventId,
                                         @NotBlank @Size(max = 255) String eventName,
                                         @NotNull UUID eventWinnerId) {

    PublishEventOutcomeCommand toCommand() {
        return new PublishEventOutcomeCommand()
                .eventId(new EventId(eventId))
                .eventName(eventName)
                .eventWinnerId(new CompetitorId(eventWinnerId));
    }
}
