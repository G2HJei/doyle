package xyz.zlatanov.doyle.core.domain.id;

import java.util.Objects;
import java.util.UUID;

public record SettlementJobId(UUID value) {

    public SettlementJobId {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
