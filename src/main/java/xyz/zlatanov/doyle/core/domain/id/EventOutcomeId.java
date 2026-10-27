package xyz.zlatanov.doyle.core.domain.id;

import java.util.Objects;
import java.util.UUID;

public record EventOutcomeId(UUID value) {

    public EventOutcomeId {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
