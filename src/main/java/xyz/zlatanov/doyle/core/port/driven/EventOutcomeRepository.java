package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventOutcome;

import java.util.UUID;

public interface EventOutcomeRepository {

    EventOutcome findOneByEventId(UUID id);

    void save(EventOutcome eventOutcome);
}
