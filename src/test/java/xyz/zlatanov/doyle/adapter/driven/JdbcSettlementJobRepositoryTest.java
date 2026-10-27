package xyz.zlatanov.doyle.adapter.driven;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import xyz.zlatanov.doyle.core.domain.*;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;

@JdbcTest
@Import({ JdbcSettlementJobRepository.class, JdbcBetRepository.class, UuidV7Service.class })
class JdbcSettlementJobRepositoryTest {

    @Autowired
    JdbcSettlementJobRepository repository;
    @Autowired
    JdbcBetRepository           betRepository;

    EventId eventId = new EventId(randomUUID());

    Bet bet = new Bet()
            .userId(new UserId(randomUUID()))
            .eventId(eventId)
            .eventMarketId(new EventMarketId(randomUUID()))
            .eventWinnerId(new CompetitorId(randomUUID()))
            .amount(new BigDecimal("10.50"))
            .status(PLACED);

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
        assertEquals(job, repository.nextPending());
    }

    @Test
    void shouldNotDuplicateJobForSameOutcomeVersion() {
        val job = job(1);
        repository.save(job);

        val repeatedJob = job(1);
        repository.save(repeatedJob);

        assertNull(repeatedJob.id());
        repository.save(job.completed(true));
        assertNull(repository.nextPending());
    }

    @Test
    void shouldCreateJobForNextOutcomeVersion() {
        val job = job(1);
        val nextVersionJob = job(2);
        repository.save(job);
        repository.save(nextVersionJob);

        repository.save(job.completed(true));

        assertEquals(nextVersionJob, repository.nextPending());
    }

    @Test
    void shouldReturnPendingJobsOrderedById() {
        val jobs = List.of(job(1), job(2), job(3));
        jobs.forEach(repository::save);

        for (val job : jobs.stream().sorted(comparing(j -> j.id().value())).toList()) {
            assertEquals(job, repository.nextPending());
            repository.save(job.completed(true));
        }
        assertNull(repository.nextPending());
    }

    @Test
    void shouldReturnNullWhenNoPendingJobs() {
        assertNull(repository.nextPending());
    }

    @Test
    void shouldUpdateExistingJob() {
        val job = job(1);
        repository.save(job);

        repository.save(job.lastBetId(bet.id()));

        assertEquals(2, job.version());
        assertEquals(job, repository.nextPending());
    }

    @Test
    void shouldRejectStaleUpdate() {
        val job = job(1);
        repository.save(job);
        val concurrentlyReadJob = repository.nextPending();
        repository.save(job.lastBetId(bet.id()));

        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(concurrentlyReadJob.completed(true)));
    }

    SettlementJob job(int outcomeVersion) {
        return new SettlementJob()
                .eventId(eventId)
                .outcomeVersion(outcomeVersion);
    }
}
