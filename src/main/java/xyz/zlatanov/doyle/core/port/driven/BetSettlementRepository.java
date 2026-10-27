package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.BetSettlement;

public interface BetSettlementRepository {

    void save(BetSettlement betSettlement);
}
