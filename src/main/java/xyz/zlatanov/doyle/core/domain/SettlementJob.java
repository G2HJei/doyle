package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class SettlementJob {

    private UUID    id;
    private UUID    eventId;
    private int     outcomeVersion;
    private boolean completed;
}
