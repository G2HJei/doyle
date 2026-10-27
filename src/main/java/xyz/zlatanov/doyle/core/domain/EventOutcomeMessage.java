package xyz.zlatanov.doyle.core.domain;

import java.util.UUID;

public record EventOutcomeMessage(UUID eventId, String eventName, UUID eventWinnerId, int version) {

    public EventOutcomeMessage(EventOutcome outcome) {
        this(outcome.eventId().value(), outcome.eventName(), outcome.eventWinnerId().value(), outcome.version());
    }
}
