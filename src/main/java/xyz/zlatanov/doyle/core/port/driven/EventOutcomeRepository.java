package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.util.List;
import java.util.Optional;

public interface EventOutcomeRepository {

    Optional<EventOutcome> findOneByEventId(EventId id);

    List<EventOutcome> findUnpublished(int limit);

    EventOutcome save(EventOutcome eventOutcome);

    void markPublished(List<EventOutcome> outcomes);
}
