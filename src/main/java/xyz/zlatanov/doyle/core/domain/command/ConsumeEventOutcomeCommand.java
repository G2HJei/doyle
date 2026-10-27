package xyz.zlatanov.doyle.core.domain.command;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.EventId;

@Data
@Accessors(fluent = true)
public class ConsumeEventOutcomeCommand {
    private EventId eventId;
    private int     outcomeVersion;
}
