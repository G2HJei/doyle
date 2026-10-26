package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.SettlementJob;

import java.util.UUID;

public interface SettlementJobRepository {

    void save(SettlementJob job);

    SettlementJob nextPending();

    void markCompleted(UUID id);
}
