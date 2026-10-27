package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.SeedBetsCommand;

import java.util.List;
import java.util.UUID;

public interface SeedBetsUseCase {

    List<UUID> seed(SeedBetsCommand command);
}
