package xyz.zlatanov.doyle.core.domain;

import java.util.Objects;
import java.util.UUID;

public record CompetitorId(UUID value) {

    public CompetitorId {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
