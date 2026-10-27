package xyz.zlatanov.doyle.core.port.driving;

import xyz.zlatanov.doyle.core.domain.EventId;

import java.util.List;
import java.util.Map;

public interface GetEventDataUseCase {

    Map<String, List<Map<String, Object>>> get(EventId eventId);
}
