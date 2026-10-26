package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;

public interface BetSettlementPublisher {

    void publish(BetSettlementMessage message);
}
