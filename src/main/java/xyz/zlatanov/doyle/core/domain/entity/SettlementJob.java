package xyz.zlatanov.doyle.core.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.SettlementJobId;

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
