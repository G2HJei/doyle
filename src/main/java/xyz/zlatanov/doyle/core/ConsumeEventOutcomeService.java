package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@RequiredArgsConstructor
public class ConsumeEventOutcomeService implements ConsumeEventOutcomeUseCase {

    private final UUIDService             uuids;
    private final SettlementJobRepository settlementJobRepository;

    @Override
    public void consumeOutcome(EventOutcome outcome) {
        settlementJobRepository.save(new SettlementJob()
                .id(uuids.get())
                .eventId(outcome.eventId()));
    }
}
