package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JdbcSettlementJobRepository implements SettlementJobRepository {

    private final JdbcClient  jdbcClient;
    private final UUIDService uuidService;

    @Override
    public void save(SettlementJob job) {
        if (job.id() == null) {
            insert(job);
        } else {
            update(job);
        }
    }

    @Override
    public SettlementJob nextPending() { // todo select for update skip locked pessimistic lock? (pointless with H2)
        return jdbcClient.sql("""
                        SELECT * FROM settlement_job
                        WHERE completed = FALSE
                        ORDER BY id
                        LIMIT 1
                        """)
                .query(this::toSettlementJob)
                .optional()
                .orElse(null);
    }

    private void insert(SettlementJob job) {
        val id = uuidService.get();
        try {
            jdbcClient.sql("""
                            INSERT INTO settlement_job (id, event_id, outcome_version, last_bet_id, completed, version)
                            VALUES (:id, :eventId, :outcomeVersion, :lastBetId, :completed, :version)
                            """)
                    .paramSource(params(job).addValue("id", id).addValue("version", 1))
                    .update();
            job.id(id).version(1);
        } catch (DuplicateKeyException e) {
            // unique (event_id, outcome_version): a repeated outcome message doesn't create another job
            log.info("Settlement job already exists: eventId={}, outcomeVersion={}", job.eventId(), job.outcomeVersion());
        }
    }

    // optimistic locking: only updates if nobody else saved the job since it was read
    private void update(SettlementJob job) {
        val updated = jdbcClient.sql("""
                        UPDATE settlement_job
                        SET event_id = :eventId, outcome_version = :outcomeVersion, last_bet_id = :lastBetId,
                            completed = :completed, version = version + 1
                        WHERE id = :id AND version = :version
                        """)
                .paramSource(params(job))
                .update();
        if (updated == 0) {
            throw new OptimisticLockingFailureException(
                    "SettlementJob %s with version %d not found".formatted(job.id(), job.version()));
        }
        job.version(job.version() + 1);
    }

    private MapSqlParameterSource params(SettlementJob job) {
        return new MapSqlParameterSource()
                .addValue("id", job.id())
                .addValue("eventId", job.eventId())
                .addValue("outcomeVersion", job.outcomeVersion())
                .addValue("lastBetId", job.lastBetId())
                .addValue("completed", job.completed())
                .addValue("version", job.version());
    }

    private SettlementJob toSettlementJob(ResultSet rs, int rowNum) throws SQLException {
        return new SettlementJob()
                .id(rs.getObject("id", UUID.class))
                .eventId(rs.getObject("event_id", UUID.class))
                .outcomeVersion(rs.getInt("outcome_version"))
                .lastBetId(rs.getObject("last_bet_id", UUID.class))
                .completed(rs.getBoolean("completed"))
                .version(rs.getInt("version"));
    }
}
