package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.BetId;
import xyz.zlatanov.doyle.core.domain.SeedBetsCommand;

import java.util.List;

public interface SeedBetsUseCase {

    List<BetId> seed(SeedBetsCommand command);
}
