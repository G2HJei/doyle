package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.BetId;
import xyz.zlatanov.doyle.core.domain.EventId;

import java.util.List;

public interface BetRepository {

    List<Bet> findByEventIdAndAfterIdAndLimit(EventId eventId, BetId afterId, int limit);

    Bet findById(BetId id);

    void save(Bet bet);
}
