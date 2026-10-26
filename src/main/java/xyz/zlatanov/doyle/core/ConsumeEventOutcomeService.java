package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@RequiredArgsConstructor
public class ConsumeEventOutcomeService implements ConsumeEventOutcomeUseCase {

    private final SettlementJobRepository settlementJobRepository;

    @Override
    public void consumeOutcome(EventOutcomeMessage outcome) {
        // todo repeated outcome triggers job each time! no-op in repo?
        settlementJobRepository.createJob(outcome.eventId(), outcome.version());
    }
}
