package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.entity.SettlementJob;

import java.util.Optional;

public interface SettlementJobRepository {

    void save(SettlementJob job);

    Optional<SettlementJob> nextPending();
}
