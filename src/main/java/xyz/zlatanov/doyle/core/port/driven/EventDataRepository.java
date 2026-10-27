package xyz.zlatanov.doyle.core.port.driven;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EventDataRepository {

    Map<String, List<Map<String, Object>>> findByEventId(UUID eventId);
}
