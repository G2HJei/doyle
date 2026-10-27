package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.entity.Bet;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.util.List;
import java.util.Optional;

public interface BetRepository {

    List<Bet> findByEventIdAndAfterIdAndLimit(EventId eventId, BetId afterId, int limit);

    Optional<Bet> findById(BetId id);

    void save(Bet bet);
}
