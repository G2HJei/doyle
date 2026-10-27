package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@RequiredArgsConstructor
public class ConsumeEventOutcomeService implements ConsumeEventOutcomeUseCase {

    private final SettlementJobRepository settlementJobRepository;

    @Transactional
    @Override
    public void consumeOutcome(EventOutcomeMessage outcomeMsg) {
        val settlementJob = toSettlementJob(outcomeMsg);
        settlementJobRepository.save(settlementJob);
    }

    private SettlementJob toSettlementJob(EventOutcomeMessage outcomeMsg) {
        return new SettlementJob()
                .eventId(outcomeMsg.eventId())
                .outcomeVersion(outcomeMsg.version());
    }
}
