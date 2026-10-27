package xyz.zlatanov.doyle.core.port.driven;

import xyz.zlatanov.doyle.core.domain.id.EventId;

import java.util.List;
import java.util.Map;

public interface EventDataRepository {

    Map<String, List<Map<String, Object>>> findByEventId(EventId eventId);
}
