package xyz.zlatanov.doyle.core.domain.command;

import xyz.zlatanov.doyle.core.domain.id.EventId;

public record ConsumeEventOutcomeCommand(EventId eventId, int outcomeVersion) {
}
