package xyz.zlatanov.doyle.adapter.driven;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.entity.BetSettlement;
import xyz.zlatanov.doyle.core.domain.entity.BetSettlement.PayoutStatus;
import xyz.zlatanov.doyle.core.domain.id.*;

import java.math.BigDecimal;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.doyle.core.domain.entity.BetSettlement.PayoutStatus.PAID_OUT;

@JdbcTest
@Import({JdbcBetSettlementRepository.class, JdbcBetRepository.class, UuidV7Provider.class})
class JdbcBetSettlementRepositoryTest {

    @Autowired
    JdbcBetSettlementRepository repository;
    @Autowired
    JdbcBetRepository           betRepository;
    @Autowired
    JdbcClient                  jdbcClient;

    Bet bet = Bet.place(new UserId(randomUUID()), new EventId(randomUUID()), new EventMarketId(randomUUID()),
            new CompetitorId(randomUUID()), new BigDecimal("10.50"));

    @BeforeEach
    void setUp() {
        betRepository.save(bet);
    }

    @Test
    void shouldInsertNewSettlement() {
        val settlement = settlement(1);

        repository.save(settlement);

        assertNotNull(settlement.id());
        assertEquals(settlement, findById(settlement.id()));
    }

    @Test
    void shouldUpdateExistingSettlement() {
        val settlement = settlement(1);
        repository.save(settlement);
        val paidOutSettlement = settlement.toBuilder().payoutStatus(PAID_OUT).build();

        repository.save(paidOutSettlement);

        assertEquals(paidOutSettlement, findById(settlement.id()));
    }

    @Test
    void shouldRejectSecondSettlementOfSameBetAndEventVersion() {
        repository.save(settlement(1));
        assertThrows(DuplicateKeyException.class, () -> repository.save(settlement(1)));
    }

    @Test
    void shouldAllowSettlementOfSameBetForNextEventVersion() {
        repository.save(settlement(1));
        assertDoesNotThrow(() -> repository.save(settlement(2)));
    }

    BetSettlement settlement(int settledEventVersion) {
        return BetSettlement.create(bet.id(), bet.amount(), settledEventVersion);
    }

    BetSettlement findById(BetSettlementId id) {
        return jdbcClient.sql("SELECT * FROM bet_settlement WHERE id = :id")
                .param("id", id.value())
                .query((rs, rowNum) -> BetSettlement.restore()
                        .id(new BetSettlementId(rs.getObject("id", UUID.class)))
                        .betId(new BetId(rs.getObject("bet_id", UUID.class)))
                        .deltaAmount(rs.getBigDecimal("delta_amount"))
                        .payoutStatus(PayoutStatus.valueOf(rs.getString("payout_status")))
                        .settledEventVersion(rs.getInt("settled_event_version"))
                        .build())
                .single();
    }
}
