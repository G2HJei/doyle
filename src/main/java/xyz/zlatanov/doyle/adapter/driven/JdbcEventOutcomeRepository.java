package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cache.annotation.CachePut;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventOutcomeId;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static xyz.zlatanov.doyle.adapter.driven.CachingEventOutcomeLookup.EVENT_OUTCOMES_CACHE;

@RequiredArgsConstructor
public class JdbcEventOutcomeRepository implements EventOutcomeRepository {

    private final JdbcClient   jdbcClient;
    private final UUIDProvider uuidService;

    @Override
    public Optional<EventOutcome> findOneByEventId(EventId eventId) {
        return jdbcClient.sql("SELECT * FROM event_outcome WHERE event_id = :eventId")
                .param("eventId", eventId.value())
                .query(this::toEventOutcome)
                .optional();
    }

    @Override
    public List<EventOutcome> findUnpublished(int limit) {
        return jdbcClient.sql("""
                        SELECT * FROM event_outcome
                        WHERE published = FALSE
                        ORDER BY id
                        LIMIT :limit
                        FOR UPDATE SKIP LOCKED
                        """)
                .param("limit", limit)
                .query(this::toEventOutcome)
                .list();
    }

    @CachePut(cacheNames = EVENT_OUTCOMES_CACHE, key = "#eventOutcome.eventId()")
    @Override
    public EventOutcome save(EventOutcome eventOutcome) {
        if (eventOutcome.id() == null) {
            insert(eventOutcome);
        } else {
            update(eventOutcome);
        }
        return eventOutcome;
    }

    // optimistic locking skips outcomes changed since they were read, leaving their new version unpublished
    @Override
    public void markPublished(List<EventOutcome> outcomes) {
        if (outcomes.isEmpty()) {
            return; // IN () is not valid SQL
        }
        val idVersions = outcomes.stream()
                .map(o -> new Object[]{o.id().value(), o.version()})
                .toList();
        jdbcClient.sql("UPDATE event_outcome SET published = TRUE WHERE (id, version) IN (:idVersions)")
                .param("idVersions", idVersions)
                .update();
    }

    private void insert(EventOutcome eventOutcome) {
        eventOutcome.assignId(new EventOutcomeId(uuidService.get()));
        eventOutcome.incrementVersion(); // stored as version 1
        jdbcClient.sql("""
                        INSERT INTO event_outcome (id, event_id, event_name, event_winner_id, published, version)
                        VALUES (:id, :eventId, :eventName, :eventWinnerId, :published, :version)
                        """)
                .paramSource(params(eventOutcome))
                .update();
    }

    // every save is a new version of the outcome, guarded by optimistic locking
    private void update(EventOutcome eventOutcome) {
        val updated = jdbcClient.sql("""
                        UPDATE event_outcome
                        SET event_id = :eventId, event_name = :eventName, event_winner_id = :eventWinnerId,
                            published = :published, version = version + 1
                        WHERE id = :id AND version = :version
                        """)
                .paramSource(params(eventOutcome))
                .update();
        if (updated == 0) {
            throw new OptimisticLockingFailureException(
                    "EventOutcome %s with version %d not found".formatted(eventOutcome.id(), eventOutcome.version()));
        }
        eventOutcome.incrementVersion();
    }

    private MapSqlParameterSource params(EventOutcome eventOutcome) {
        return new MapSqlParameterSource()
                .addValue("id", eventOutcome.id().value())
                .addValue("eventId", eventOutcome.eventId().value())
                .addValue("eventName", eventOutcome.eventName())
                .addValue("eventWinnerId", eventOutcome.eventWinnerId().value())
                .addValue("published", eventOutcome.published())
                .addValue("version", eventOutcome.version());
    }

    private EventOutcome toEventOutcome(ResultSet rs, int rowNum) throws SQLException {
        return EventOutcome.restore()
                .id(new EventOutcomeId(rs.getObject("id", UUID.class)))
                .eventId(new EventId(rs.getObject("event_id", UUID.class)))
                .eventName(rs.getString("event_name"))
                .eventWinnerId(new CompetitorId(rs.getObject("event_winner_id", UUID.class)))
                .published(rs.getBoolean("published"))
                .version(rs.getInt("version"))
                .build();
    }
}
