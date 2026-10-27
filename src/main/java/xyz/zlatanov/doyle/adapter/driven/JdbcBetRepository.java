package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.*;
import xyz.zlatanov.doyle.core.domain.Bet.BetResult;
import xyz.zlatanov.doyle.core.domain.Bet.BetStatus;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class JdbcBetRepository implements BetRepository {

    private final JdbcClient  jdbcClient;
    private final UUIDService uuidService;

    @Override
    public List<Bet> findByEventIdAndAfterIdAndLimit(EventId eventId, BetId afterId, int limit) {
        return jdbcClient.sql("""
                        SELECT * FROM bet
                        WHERE event_id = :eventId AND (CAST(:afterId AS UUID) IS NULL OR id > :afterId)
                        ORDER BY id
                        LIMIT :limit
                        """)
                .param("eventId", eventId.value())
                .param("afterId", afterId == null ? null : afterId.value())
                .param("limit", limit)
                .query(this::toBet)
                .list();
    }

    @Override
    public Bet findById(BetId id) {
        return jdbcClient.sql("SELECT * FROM bet WHERE id = :id")
                .param("id", id.value())
                .query(this::toBet)
                .optional()
                .orElse(null);
    }

    @Override
    public void save(Bet bet) {
        if (bet.id() == null) {
            insert(bet);
        } else {
            update(bet);
        }
    }

    private void insert(Bet bet) {
        bet.id(new BetId(uuidService.get())).version(1);
        jdbcClient.sql("""
                        INSERT INTO bet (id, user_id, event_id, event_market_id, event_winner_id, amount,
                                         status, result, settled_event_version, version)
                        VALUES (:id, :userId, :eventId, :eventMarketId, :eventWinnerId, :amount,
                                :status, :result, :settledEventVersion, :version)
                        """)
                .paramSource(params(bet))
                .update();
    }

    // optimistic locking: only updates if nobody else saved the bet since it was read
    private void update(Bet bet) {
        val updated = jdbcClient.sql("""
                        UPDATE bet
                        SET user_id = :userId, event_id = :eventId, event_market_id = :eventMarketId,
                            event_winner_id = :eventWinnerId, amount = :amount, status = :status, result = :result,
                            settled_event_version = :settledEventVersion, version = version + 1
                        WHERE id = :id AND version = :version
                        """)
                .paramSource(params(bet))
                .update();
        if (updated == 0) {
            throw new OptimisticLockingFailureException(
                    "Bet %s with version %d not found".formatted(bet.id(), bet.version()));
        }
        bet.version(bet.version() + 1);
    }

    private MapSqlParameterSource params(Bet bet) {
        return new MapSqlParameterSource()
                .addValue("id", bet.id().value())
                .addValue("userId", bet.userId().value())
                .addValue("eventId", bet.eventId().value())
                .addValue("eventMarketId", bet.eventMarketId().value())
                .addValue("eventWinnerId", bet.eventWinnerId().value())
                .addValue("amount", bet.amount())
                .addValue("status", bet.status().name())
                .addValue("result", bet.result() == null ? null : bet.result().name())
                .addValue("settledEventVersion", bet.settledEventVersion())
                .addValue("version", bet.version());
    }

    private Bet toBet(ResultSet rs, int rowNum) throws SQLException {
        val result = rs.getString("result");
        return new Bet()
                .id(new BetId(rs.getObject("id", UUID.class)))
                .userId(new UserId(rs.getObject("user_id", UUID.class)))
                .eventId(new EventId(rs.getObject("event_id", UUID.class)))
                .eventMarketId(new EventMarketId(rs.getObject("event_market_id", UUID.class)))
                .eventWinnerId(new CompetitorId(rs.getObject("event_winner_id", UUID.class)))
                .amount(rs.getBigDecimal("amount"))
                .status(BetStatus.valueOf(rs.getString("status")))
                .result(result == null ? null : BetResult.valueOf(result))
                .settledEventVersion(rs.getObject("settled_event_version", Integer.class))
                .version(rs.getInt("version"));
    }
}
