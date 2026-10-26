package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@RequiredArgsConstructor
public class ConsumeEventOutcomeService implements ConsumeEventOutcomeUseCase {

    private final SettlementJobRepository settlementJobRepository;

    @Override
    public void consumeOutcome(EventOutcomeMessage outcomeMsg) {
        // todo repeated outcome message for the same event version to be deduped in the repository
        val settlementJob = toSettlementJob(outcomeMsg);
        settlementJobRepository.save(settlementJob);
    }

    private SettlementJob toSettlementJob(EventOutcomeMessage outcomeMsg) {
        return new SettlementJob()
                .eventId(outcomeMsg.eventId())
                .version(outcomeMsg.version());
    }
}
