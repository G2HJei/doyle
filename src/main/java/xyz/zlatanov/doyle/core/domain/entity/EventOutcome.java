package xyz.zlatanov.doyle.core.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventOutcomeId;

@Data
@Accessors(fluent = true)
public class EventOutcome {
    private EventOutcomeId id;
    private EventId        eventId;
    private String         eventName;
    private CompetitorId   eventWinnerId;
    private boolean        published; //todo can move to a dedicated outbox table
    private Integer        version;
}
