package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.command.ConsumeEventOutcomeCommand;
import xyz.zlatanov.doyle.core.domain.entity.SettlementJob;
import xyz.zlatanov.doyle.core.port.driven.SettlementJobRepository;
import xyz.zlatanov.doyle.core.port.driving.ConsumeEventOutcomeUseCase;

@Slf4j
@RequiredArgsConstructor
public class ConsumeEventOutcomeCommandProcessor implements ConsumeEventOutcomeUseCase {

    private final SettlementJobRepository settlementJobRepository;

    @Transactional
    @Override
    public void consumeOutcome(ConsumeEventOutcomeCommand command) {
        val settlementJob = toSettlementJob(command);
        settlementJobRepository.save(settlementJob);
        log.info("Event outcome consumed: eventId={}, outcomeVersion={}", command.eventId(), command.outcomeVersion());
    }

    private SettlementJob toSettlementJob(ConsumeEventOutcomeCommand command) {
        return new SettlementJob()
                .eventId(command.eventId())
                .outcomeVersion(command.outcomeVersion())
                .lastBetId(null)
                .completed(false);
    }
}
