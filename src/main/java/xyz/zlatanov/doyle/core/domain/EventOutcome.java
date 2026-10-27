package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class EventOutcome {
    private UUID    id;
    private UUID    eventId;
    private String  eventName;
    private UUID    eventWinnerId;
    private boolean published; //todo can move to a dedicated outbox table
    private Integer version;
}
