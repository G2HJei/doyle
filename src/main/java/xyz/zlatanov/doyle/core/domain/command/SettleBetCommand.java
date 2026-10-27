package xyz.zlatanov.doyle.core.domain.command;

import xyz.zlatanov.doyle.core.domain.id.BetId;

public record SettleBetCommand(BetId betId) {
}
