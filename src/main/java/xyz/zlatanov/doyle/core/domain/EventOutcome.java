package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

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
