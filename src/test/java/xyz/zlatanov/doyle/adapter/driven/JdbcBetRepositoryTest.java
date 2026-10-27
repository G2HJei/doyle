package xyz.zlatanov.doyle.adapter.driven;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.id.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({JdbcBetRepository.class, UuidV7Provider.class})
class JdbcBetRepositoryTest {

    @Autowired
    JdbcBetRepository repository;

    EventId eventId = new EventId(randomUUID());

    @Test
    void shouldInsertNewBet() {
        val bet = bet(eventId);

        repository.save(bet);

        assertNotNull(bet.id());
        assertEquals(1, bet.version());
        assertEquals(Optional.of(bet), repository.findById(bet.id()));
    }

    @Test
    void shouldUpdateExistingBet() {
        val bet = bet(eventId);
        repository.save(bet);

        bet.settle(winningOutcome(bet));
        repository.save(bet);

        assertEquals(2, bet.version());
        assertEquals(Optional.of(bet), repository.findById(bet.id()));
    }

    @Test
    void shouldRejectStaleUpdate() {
        val bet = bet(eventId);
        repository.save(bet);
        val concurrentlyReadBet = repository.findById(bet.id()).orElseThrow();
        bet.settle(winningOutcome(bet));
        repository.save(bet);

        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(concurrentlyReadBet));
    }

    @Test
    void shouldReturnEmptyForUnknownBet() {
        assertEquals(Optional.empty(), repository.findById(new BetId(randomUUID())));
    }

    @Test
    void shouldPageThroughEventBetsOrderedById() {
        val bets = List.of(bet(eventId), bet(new EventId(randomUUID())), bet(eventId), bet(eventId));
        bets.forEach(repository::save);
        val eventBets = bets.stream()
                .filter(b -> eventId.equals(b.eventId()))
                .sorted(comparing(b -> b.id().value()))
                .toList();

        assertEquals(eventBets.subList(0, 2), repository.findByEventIdAndAfterIdAndLimit(eventId, null, 2));
        assertEquals(eventBets.subList(2, 3),
                repository.findByEventIdAndAfterIdAndLimit(eventId, eventBets.get(1).id(), 2));
        assertEquals(List.of(), repository.findByEventIdAndAfterIdAndLimit(eventId, eventBets.get(2).id(), 2));
    }

    Bet bet(EventId eventId) {
        return Bet.place(new UserId(randomUUID()), eventId, new EventMarketId(randomUUID()),
                new CompetitorId(randomUUID()), new BigDecimal("10.50"));
    }

    EventOutcome winningOutcome(Bet bet) {
        return EventOutcome.restore()
                .eventId(bet.eventId())
                .eventWinnerId(bet.eventWinnerId())
                .version(1)
                .build();
    }
}
