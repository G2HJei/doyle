package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ConsumeEventOutcomeCommand {
    private EventId eventId;
    private int     outcomeVersion;
}
