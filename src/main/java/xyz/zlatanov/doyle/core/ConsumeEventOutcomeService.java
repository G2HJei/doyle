package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@Slf4j
@RequiredArgsConstructor
public class ConsumeEventOutcomeService implements ConsumeEventOutcomeUseCase {

    private final SettlementJobRepository settlementJobRepository;

    @Transactional
    @Override
    public void consumeOutcome(EventOutcomeMessage outcomeMsg) {
        val settlementJob = toSettlementJob(outcomeMsg);
        settlementJobRepository.save(settlementJob);
        log.info("Event outcome consumed: eventId={}, outcomeVersion={}", outcomeMsg.eventId(), outcomeMsg.version());
    }

    private SettlementJob toSettlementJob(EventOutcomeMessage outcomeMsg) {
        return new SettlementJob()
                .eventId(outcomeMsg.eventId())
                .outcomeVersion(outcomeMsg.version())
                .lastBetId(null)
                .completed(false);
    }
}
