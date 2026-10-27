package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SettlementJob {

    private SettlementJobId id;
    private EventId         eventId;
    private int             outcomeVersion;
    private BetId           lastBetId;
    private boolean         completed;
    private int             version;
}
