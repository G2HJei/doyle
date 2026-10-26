package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.SettlementJob;

public interface SettlementJobRepository {
    
    void save(SettlementJob settlementJob);
}
