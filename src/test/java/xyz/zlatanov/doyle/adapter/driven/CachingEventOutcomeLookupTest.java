package xyz.zlatanov.doyle.adapter.driven;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import xyz.zlatanov.doyle.config.CachingConfig;
import xyz.zlatanov.doyle.core.domain.CompetitorId;
import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringJUnitConfig({ CachingConfig.class, CachingEventOutcomeLookup.class })
class CachingEventOutcomeLookupTest {

    @MockitoBean
    EventOutcomeRepository repository;
    @Autowired
    EventOutcomeLookup     lookup;

    EventOutcome outcome = new EventOutcome()
            .eventId(new EventId(randomUUID()))
            .eventName("test event")
            .eventWinnerId(new CompetitorId(randomUUID()))
            .published(false)
            .version(1);

    @Test
    void shouldLoadOutcomeOnceAndServeItFromCache() {
        when(repository.findOneByEventId(outcome.eventId())).thenReturn(outcome);

        lookup.findOneByEventId(outcome.eventId());

        assertEquals(outcome, lookup.findOneByEventId(outcome.eventId()));
        verify(repository, times(1)).findOneByEventId(outcome.eventId());
    }
}
