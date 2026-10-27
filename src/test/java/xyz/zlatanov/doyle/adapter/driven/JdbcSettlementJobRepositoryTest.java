package xyz.zlatanov.doyle.adapter.driven;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.entity.SettlementJob;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventMarketId;
import xyz.zlatanov.doyle.core.domain.id.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({JdbcSettlementJobRepository.class, JdbcBetRepository.class, UuidV7Provider.class})
class JdbcSettlementJobRepositoryTest {

    @Autowired
    JdbcSettlementJobRepository repository;
    @Autowired
    JdbcBetRepository           betRepository;

    EventId eventId = new EventId(randomUUID());

    Bet bet = Bet.place(new UserId(randomUUID()), eventId, new EventMarketId(randomUUID()),
            new CompetitorId(randomUUID()), new BigDecimal("10.50"));

    @BeforeEach
    void setUp() {
        betRepository.save(bet);
    }

    @Test
    void shouldInsertNewJob() {
        val job = job(1);

        repository.save(job);

        assertNotNull(job.id());
        assertEquals(1, job.version());
        assertEquals(Optional.of(job), repository.nextPending());
    }

    @Test
    void shouldNotDuplicateJobForSameOutcomeVersion() {
        val job = job(1);
        repository.save(job);

        val repeatedJob = job(1);
        repository.save(repeatedJob);

        assertNull(repeatedJob.id());
        job.complete();
        repository.save(job);
        assertEquals(Optional.empty(), repository.nextPending());
    }

    @Test
    void shouldCreateJobForNextOutcomeVersion() {
        val job = job(1);
        val nextVersionJob = job(2);
        repository.save(job);
        repository.save(nextVersionJob);

        job.complete();
        repository.save(job);

        assertEquals(Optional.of(nextVersionJob), repository.nextPending());
    }

    @Test
    void shouldReturnPendingJobsOrderedById() {
        val jobs = List.of(job(1), job(2), job(3));
        jobs.forEach(repository::save);

        for (val job : jobs.stream().sorted(comparing(j -> j.id().value())).toList()) {
            assertEquals(Optional.of(job), repository.nextPending());
            job.complete();
            repository.save(job);
        }
        assertEquals(Optional.empty(), repository.nextPending());
    }

    @Test
    void shouldReturnEmptyWhenNoPendingJobs() {
        assertEquals(Optional.empty(), repository.nextPending());
    }

    @Test
    void shouldUpdateExistingJob() {
        val job = job(1);
        repository.save(job);

        job.recordProgress(bet.id());
        repository.save(job);

        assertEquals(2, job.version());
        assertEquals(Optional.of(job), repository.nextPending());
    }

    @Test
    void shouldRejectStaleUpdate() {
        val job = job(1);
        repository.save(job);
        val concurrentlyReadJob = repository.nextPending().orElseThrow();
        job.recordProgress(bet.id());
        repository.save(job);
        concurrentlyReadJob.complete();

        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(concurrentlyReadJob));
    }

    SettlementJob job(int outcomeVersion) {
        return SettlementJob.create(eventId, outcomeVersion);
    }
}
