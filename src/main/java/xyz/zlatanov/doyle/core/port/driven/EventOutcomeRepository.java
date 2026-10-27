package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.EventOutcome;

import java.util.List;
import java.util.UUID;

public interface EventOutcomeRepository {

    EventOutcome findOneByEventId(UUID id);

    List<EventOutcome> findUnpublished(int limit);

    void save(EventOutcome eventOutcome);

    void markPublished(List<EventOutcome> outcomes);
}
