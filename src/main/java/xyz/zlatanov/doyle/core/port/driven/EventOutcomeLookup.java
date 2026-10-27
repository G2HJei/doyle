package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.util.Optional;

public interface EventOutcomeLookup {

    Optional<EventOutcome> findOneByEventId(EventId eventId);
}
