package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.entity.BetSettlement;

public interface BetSettlementRepository {

    void save(BetSettlement betSettlement);
}
