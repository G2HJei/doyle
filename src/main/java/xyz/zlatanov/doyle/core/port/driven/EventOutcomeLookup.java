package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventOutcome;

import java.util.UUID;

public interface EventOutcomeLookup {

    EventOutcome getEventOutcome(UUID eventId);
}
