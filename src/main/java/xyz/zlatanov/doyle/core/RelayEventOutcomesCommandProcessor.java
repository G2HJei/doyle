package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;
import xyz.zlatanov.doyle.core.port.driving.RelayEventOutcomesUseCase;

@RequiredArgsConstructor
public class RelayEventOutcomesCommandProcessor implements RelayEventOutcomesUseCase {

    private final EventOutcomeRepository outcomeRepository;
    private final EventOutcomePublisher  publisher;

    @Override
    public void relayOutcomes() {
        outcomeRepository.findUnpublished(0);
    }
}
