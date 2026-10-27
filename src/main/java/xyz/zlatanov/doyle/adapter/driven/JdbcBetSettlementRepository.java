package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.BetSettlement;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

@RequiredArgsConstructor
public class JdbcBetSettlementRepository implements BetSettlementRepository {

    private final JdbcClient  jdbcClient;
    private final UUIDService uuidService;

    @Override
    public void save(BetSettlement betSettlement) {
        if (betSettlement.id() == null) {
            insert(betSettlement);
        } else {
            update(betSettlement);
        }
    }

    private void insert(BetSettlement betSettlement) {
        betSettlement.id(uuidService.get());
        jdbcClient.sql("""
                        INSERT INTO bet_settlement (id, bet_id, delta_amount, payout_status, settled_event_version)
                        VALUES (:id, :betId, :deltaAmount, :payoutStatus, :settledEventVersion)
                        """)
                .paramSource(params(betSettlement))
                .update();
    }

    private void update(BetSettlement betSettlement) {
        val updated = jdbcClient.sql("""
                        UPDATE bet_settlement
                        SET bet_id = :betId, delta_amount = :deltaAmount, payout_status = :payoutStatus,
                            settled_event_version = :settledEventVersion
                        WHERE id = :id
                        """)
                .paramSource(params(betSettlement))
                .update();
        if (updated == 0) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    "BetSettlement %s not found".formatted(betSettlement.id()));
        }
    }

    private MapSqlParameterSource params(BetSettlement betSettlement) {
        return new MapSqlParameterSource()
                .addValue("id", betSettlement.id())
                .addValue("betId", betSettlement.betId())
                .addValue("deltaAmount", betSettlement.deltaAmount())
                .addValue("payoutStatus", betSettlement.payoutStatus().name())
                .addValue("settledEventVersion", betSettlement.settledEventVersion());
    }
}
