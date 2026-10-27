package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;

@RequiredArgsConstructor
public class CachingEventOutcomeLookup implements EventOutcomeLookup {

    public static final String EVENT_OUTCOMES_CACHE = "eventOutcomes";

    private final EventOutcomeRepository repository;

    @Cacheable(cacheNames = EVENT_OUTCOMES_CACHE, sync = true) //sync to make concurrent write wait the load to finish
    @Override
    public EventOutcome findOneByEventId(EventId eventId) {
        return repository.findOneByEventId(eventId);
    }
}
