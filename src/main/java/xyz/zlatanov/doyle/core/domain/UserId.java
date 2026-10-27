package xyz.zlatanov.doyle.core.domain;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
