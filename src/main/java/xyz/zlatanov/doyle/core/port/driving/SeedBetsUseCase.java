package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.command.SeedBetsCommand;
import xyz.zlatanov.doyle.core.domain.id.BetId;

import java.util.List;

public interface SeedBetsUseCase {

    List<BetId> seed(SeedBetsCommand command);
}
