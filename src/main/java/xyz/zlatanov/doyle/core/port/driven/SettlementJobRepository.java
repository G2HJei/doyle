package xyz.zlatanov.doyle.core.port.driven;

import java.util.UUID;

public interface SettlementJobRepository {

    void create(UUID eventId);
}
