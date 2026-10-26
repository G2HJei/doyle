package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.Bet;

import java.util.List;
import java.util.UUID;

public interface BetRepository {

    List<Bet> findByEventIdAndAfterId(UUID eventId, UUID afterId);
}
