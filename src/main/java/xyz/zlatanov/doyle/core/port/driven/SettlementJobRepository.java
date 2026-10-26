package xyz.zlatanov.doyle.core.port.driven;

import java.util.UUID;

public interface SettlementJobRepository {

    void createJob(UUID eventId, int eventVersion);
}
