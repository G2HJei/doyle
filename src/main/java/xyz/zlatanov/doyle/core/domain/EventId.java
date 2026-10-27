package xyz.zlatanov.doyle.core.domain;

import java.util.Objects;
import java.util.UUID;

public record EventId(UUID value) {

    public EventId {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
