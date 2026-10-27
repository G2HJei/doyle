package xyz.zlatanov.doyle.adapter.driven;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({JdbcEventOutcomeRepository.class, UuidV7Service.class})
class JdbcEventOutcomeRepositoryTest {

    @Autowired
    JdbcEventOutcomeRepository repository;

    @Test
    void shouldInsertNewOutcomeAsFirstVersion() {
        val outcome = outcome();

        repository.save(outcome);

        assertNotNull(outcome.id());
        assertEquals(1, outcome.version());
        assertEquals(Optional.of(outcome), repository.findOneByEventId(outcome.eventId()));
    }

    @Test
    void shouldSaveChangedOutcomeAsNextVersion() {
        val outcome = outcome();
        repository.save(outcome);

        repository.save(outcome.eventWinnerId(new CompetitorId(randomUUID())));

        assertEquals(2, outcome.version());
        assertEquals(Optional.of(outcome), repository.findOneByEventId(outcome.eventId()));
    }

    @Test
    void shouldRejectStaleUpdate() {
        val outcome = outcome();
        repository.save(outcome);
        val concurrentlyReadOutcome = repository.findOneByEventId(outcome.eventId()).orElseThrow();
        repository.save(outcome.eventWinnerId(new CompetitorId(randomUUID())));

        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(concurrentlyReadOutcome));
    }

    @Test
    void shouldRejectSecondOutcomeForSameEvent() {
        val outcome = outcome();
        repository.save(outcome);

        assertThrows(DuplicateKeyException.class, () -> repository.save(outcome().eventId(outcome.eventId())));
    }

    @Test
    void shouldReturnEmptyForUnknownEvent() {
        assertEquals(Optional.empty(), repository.findOneByEventId(new EventId(randomUUID())));
    }

    @Test
    void shouldFindUnpublishedOrderedByIdUpToLimit() {
        val outcomes = List.of(outcome(), outcome().published(true), outcome(), outcome());
        outcomes.forEach(repository::save);
        val unpublished = outcomes.stream()
                .filter(o -> !o.published())
                .sorted(comparing(o -> o.id().value()))
                .toList();

        assertEquals(unpublished.subList(0, 2), repository.findUnpublished(2));
    }

    @Test
    void shouldMarkPublishedWithoutChangingVersion() {
        val outcome_1 = outcome();
        val outcome_2 = outcome();
        List.of(outcome_1, outcome_2).forEach(repository::save);

        repository.markPublished(List.of(outcome_1));

        assertEquals(List.of(outcome_2), repository.findUnpublished(10));
        assertEquals(Optional.of(outcome_1.published(true)), repository.findOneByEventId(outcome_1.eventId()));
    }

    @Test
    void shouldNotMarkPublishedStaleOutcome() {
        val outcome_1 = outcome();
        val outcome_2 = outcome();
        List.of(outcome_1, outcome_2).forEach(repository::save);
        val staleOutcome_2 = repository.findOneByEventId(outcome_2.eventId()).orElseThrow();
        repository.save(outcome_2.eventWinnerId(new CompetitorId(randomUUID())));

        repository.markPublished(List.of(outcome_1, staleOutcome_2));

        assertEquals(List.of(outcome_2), repository.findUnpublished(10));
    }

    @Test
    void shouldIgnoreMarkPublishedOfNoOutcomes() {
        assertDoesNotThrow(() -> repository.markPublished(List.of()));
    }

    EventOutcome outcome() {
        return new EventOutcome()
                .eventId(new EventId(randomUUID()))
                .eventName("test event")
                .eventWinnerId(new CompetitorId(randomUUID()))
                .published(false);
    }
}
