package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.EventId;
import xyz.zlatanov.doyle.core.port.driven.EventDataRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public class JdbcEventDataRepository implements EventDataRepository {

    private final JdbcClient jdbcClient;

    @Override
    public Map<String, List<Map<String, Object>>> findByEventId(EventId eventId) {
        val data = new LinkedHashMap<String, List<Map<String, Object>>>();
        data.put("event_outcome", rows("""
                SELECT * FROM event_outcome
                WHERE CAST(:eventId AS UUID) IS NULL OR event_id = :eventId
                ORDER BY id
                """, eventId));
        data.put("bet", rows("""
                SELECT * FROM bet
                WHERE CAST(:eventId AS UUID) IS NULL OR event_id = :eventId
                ORDER BY id
                """, eventId));
        data.put("bet_settlement", rows("""
                SELECT * FROM bet_settlement
                WHERE bet_id IN (SELECT id FROM bet WHERE CAST(:eventId AS UUID) IS NULL OR event_id = :eventId)
                ORDER BY id
                """, eventId));
        data.put("settlement_job", rows("""
                SELECT * FROM settlement_job
                WHERE CAST(:eventId AS UUID) IS NULL OR event_id = :eventId
                ORDER BY id
                """, eventId));
        return data;
    }

    private List<Map<String, Object>> rows(String sql, EventId eventId) {
        return jdbcClient.sql(sql)
                .param("eventId", eventId == null ? null : eventId.value())
                .query(this::toRow)
                .list();
    }

    private Map<String, Object> toRow(ResultSet rs, int rowNum) throws SQLException {
        val metaData = rs.getMetaData();
        val row = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            // H2 reports column names in upper case, lower them to match schema.sql
            row.put(metaData.getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
        }
        return row;
    }
}
