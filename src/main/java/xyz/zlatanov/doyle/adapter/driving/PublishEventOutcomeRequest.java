package xyz.zlatanov.doyle.adapter.driving;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import xyz.zlatanov.doyle.core.domain.command.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.util.UUID;

public record PublishEventOutcomeRequest(@NotNull UUID eventId,
                                         @NotBlank @Size(max = 255) String eventName,
                                         @NotNull UUID eventWinnerId) {

    PublishEventOutcomeCommand toCommand() {
        return new PublishEventOutcomeCommand(new EventId(eventId), eventName, new CompetitorId(eventWinnerId));
    }
}
