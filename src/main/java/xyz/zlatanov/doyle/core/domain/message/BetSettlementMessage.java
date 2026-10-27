package xyz.zlatanov.doyle.core.domain.message;

import xyz.zlatanov.doyle.core.domain.entity.Bet;

import java.util.UUID;

public record BetSettlementMessage(UUID betId, UUID userId) {

    public BetSettlementMessage(Bet bet) {
        this(bet.id().value(), bet.userId().value());
    }
}
