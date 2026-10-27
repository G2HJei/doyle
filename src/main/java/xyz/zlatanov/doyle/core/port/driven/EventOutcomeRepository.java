package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.domain.EventOutcome;

import java.util.List;

public interface EventOutcomeRepository {

    EventOutcome findOneByEventId(EventId id);

    List<EventOutcome> findUnpublished(int limit);

    EventOutcome save(EventOutcome eventOutcome);

    void markPublished(List<EventOutcome> outcomes);
}
