package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PublishEventOutcomeCommand {

    private EventId      eventId;
    private String       eventName;
    private CompetitorId eventWinnerId;
}
