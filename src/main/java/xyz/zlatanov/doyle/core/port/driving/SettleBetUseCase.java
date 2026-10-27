package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.command.SettleBetCommand;

public interface SettleBetUseCase {

    void settle(SettleBetCommand command);
}
