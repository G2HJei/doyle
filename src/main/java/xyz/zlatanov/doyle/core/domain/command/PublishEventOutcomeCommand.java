package xyz.zlatanov.doyle.core.domain.command;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;

@Data
@Accessors(fluent = true)
public class PublishEventOutcomeCommand {

    private EventId      eventId;
    private String       eventName;
    private CompetitorId eventWinnerId;
}
