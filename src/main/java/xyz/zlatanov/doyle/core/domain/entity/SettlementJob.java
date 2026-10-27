package xyz.zlatanov.doyle.core.domain.entity;

import lombok.*;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.SettlementJobId;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@Builder(builderMethodName = "restore", toBuilder = true) // restores stored jobs; new ones are created
@AllArgsConstructor(access = PRIVATE)
public class SettlementJob {

    private SettlementJobId id;
    private EventId         eventId;
    private int             outcomeVersion;
    private BetId           lastBetId;
    private boolean         completed;
    private int             version;

    public static SettlementJob create(EventId eventId, int outcomeVersion) {
        return new SettlementJobBuilder()
                .eventId(eventId)
                .outcomeVersion(outcomeVersion)
                .lastBetId(null)
                .completed(false)
                .build();
    }

    public void recordProgress(BetId lastBetId) {
        this.lastBetId = lastBetId;
    }

    public void complete() {
        completed = true;
    }

    public void assignId(SettlementJobId id) {
        assert this.id == null : "Settlement job already has an id";
        this.id = id;
    }

    public void incrementVersion() {
        version++;
    }
}
