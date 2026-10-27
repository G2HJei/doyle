package xyz.zlatanov.doyle.acceptance.dsl;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.*;

@Getter
@Accessors(fluent = true)
public class TestContext {
    private final UUID                    eventId            = UUID.randomUUID();
    private final UUID                    eventMarketId      = UUID.randomUUID();
    private final Map<String, UUID>       competitorIds      = new HashMap<>();
    private final Map<String, List<UUID>> betIdsByCompetitor = new HashMap<>();

    public UUID competitorId(String competitor) {
        return competitorIds.computeIfAbsent(competitor, c -> UUID.randomUUID());
    }

    public List<UUID> competitorBetIds(String competitor) {
        return betIdsByCompetitor.computeIfAbsent(competitor, c -> new ArrayList<>());
    }
}
