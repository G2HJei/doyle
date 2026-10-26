package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.SettleBetCommand;

public interface SettleBetUseCase {

    void settle(SettleBetCommand command);
}
