package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class JdbcEventOutcomeRepository implements EventOutcomeRepository {

    private final JdbcClient  jdbcClient;
    private final UUIDService uuidService;

    @Override
    public EventOutcome findOneByEventId(UUID eventId) {
        return jdbcClient.sql("SELECT * FROM event_outcome WHERE event_id = :eventId")
                .param("eventId", eventId)
                .query(this::toEventOutcome)
                .optional()
                .orElse(null);
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

    @Override
    public void save(EventOutcome eventOutcome) {
        if (eventOutcome.id() == null) {
            insert(eventOutcome);
        } else {
            update(eventOutcome);
        }
    }

    // optimistic locking skips outcomes changed since they were read, leaving their new version unpublished
    @Override
    public void markPublished(List<EventOutcome> outcomes) {
        if (outcomes.isEmpty()) {
            return; // IN () is not valid SQL
        }
        val idVersions = outcomes.stream()
                .map(o -> new Object[]{o.id(), o.version()})
                .toList();
        jdbcClient.sql("UPDATE event_outcome SET published = TRUE WHERE (id, version) IN (:idVersions)")
                .param("idVersions", idVersions)
                .update();
    }

    private void insert(EventOutcome eventOutcome) {
        eventOutcome.id(uuidService.get()).version(1);
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
        eventOutcome.version(eventOutcome.version() + 1);
    }

    private MapSqlParameterSource params(EventOutcome eventOutcome) {
        return new MapSqlParameterSource()
                .addValue("id", eventOutcome.id())
                .addValue("eventId", eventOutcome.eventId())
                .addValue("eventName", eventOutcome.eventName())
                .addValue("eventWinnerId", eventOutcome.eventWinnerId())
                .addValue("published", eventOutcome.published())
                .addValue("version", eventOutcome.version());
    }

    private EventOutcome toEventOutcome(ResultSet rs, int rowNum) throws SQLException {
        return new EventOutcome()
                .id(rs.getObject("id", UUID.class))
                .eventId(rs.getObject("event_id", UUID.class))
                .eventName(rs.getString("event_name"))
                .eventWinnerId(rs.getObject("event_winner_id", UUID.class))
                .published(rs.getBoolean("published"))
                .version(rs.getInt("version"));
    }
}
