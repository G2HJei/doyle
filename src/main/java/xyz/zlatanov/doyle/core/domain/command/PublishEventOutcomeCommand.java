package xyz.zlatanov.doyle.core.domain.command;

import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;

public record PublishEventOutcomeCommand(EventId eventId, String eventName, CompetitorId eventWinnerId) {
}
