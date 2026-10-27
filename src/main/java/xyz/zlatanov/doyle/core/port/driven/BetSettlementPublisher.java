package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.message.BetSettlementMessage;

public interface BetSettlementPublisher {

    void publish(BetSettlementMessage message);
}
