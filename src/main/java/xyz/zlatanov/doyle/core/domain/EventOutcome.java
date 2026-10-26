package xyz.zlatanov.doyle.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class EventOutcome {
    private UUID   id;
    private String name;
    private UUID   winnerId;
}
