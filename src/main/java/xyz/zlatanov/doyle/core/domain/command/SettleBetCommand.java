package xyz.zlatanov.doyle.core.domain.command;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.doyle.core.domain.id.BetId;

@Data
@Accessors(fluent = true)
public class SettleBetCommand {
    private BetId betId;
}
