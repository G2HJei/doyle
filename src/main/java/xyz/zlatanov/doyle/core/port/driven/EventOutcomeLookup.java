package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.domain.EventOutcome;

public interface EventOutcomeLookup {

    EventOutcome findOneByEventId(EventId eventId);
}
