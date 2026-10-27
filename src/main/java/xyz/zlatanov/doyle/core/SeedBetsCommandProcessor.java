package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.SeedBetsCommand;
import xyz.zlatanov.doyle.core.domain.SeedBetsCommand.BetTemplate;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driving.SeedBetsUseCase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;

@RequiredArgsConstructor
public class SeedBetsCommandProcessor implements SeedBetsUseCase {

    private final BetRepository betRepository;

    @Transactional
    @Override
    public List<UUID> seed(SeedBetsCommand command) { // command returning for POC purposes only
        return Stream.generate(() -> toBet(command.template()))
                .limit(command.createCount())
                .peek(betRepository::save) // for POC purposes performance here is irrelevant
                .map(Bet::id)
                .toList();
    }

    private Bet toBet(BetTemplate template) {
        return new Bet()
                .userId(template.userId())
                .eventId(template.eventId())
                .eventMarketId(template.eventMarketId())
                .eventWinnerId(template.eventWinnerId())
                .amount(template.amount())
                .status(PLACED);
    }
}
