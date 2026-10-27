package xyz.zlatanov.doyle.core.domain.entity;

import lombok.*;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventOutcomeId;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@Builder(builderMethodName = "restore", toBuilder = true) // restores stored outcomes; new ones are created
@AllArgsConstructor(access = PRIVATE)
public class EventOutcome {
    private EventOutcomeId id;
    private EventId        eventId;
    private String         eventName;
    private CompetitorId   eventWinnerId;
    private boolean        published; //todo can move to a dedicated outbox table
    private int            version;

    public static EventOutcome create(EventId eventId, String eventName, CompetitorId eventWinnerId) {
        return new EventOutcomeBuilder()
                .eventId(eventId)
                .eventName(eventName)
                .eventWinnerId(eventWinnerId)
                .published(false)
                .build();
    }

    public void correct(String eventName, CompetitorId eventWinnerId) {
        this.eventName = eventName;
        this.eventWinnerId = eventWinnerId;
        published = false;
    }

    public void assignId(EventOutcomeId id) {
        assert this.id == null : "Event outcome already has an id";
        this.id = id;
    }

    public void incrementVersion() {
        version++;
    }
}
