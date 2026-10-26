package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.Bet;

import java.util.List;
import java.util.UUID;

public interface BetRepository {

    List<Bet> findByEventIdAndAfterIdAndLimit(UUID eventId, UUID afterId, int limit);
}
