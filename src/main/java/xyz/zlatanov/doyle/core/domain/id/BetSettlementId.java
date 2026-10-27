package xyz.zlatanov.doyle.core.domain.id;

import java.util.Objects;
import java.util.UUID;

public record BetSettlementId(UUID value) {

    public BetSettlementId {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
