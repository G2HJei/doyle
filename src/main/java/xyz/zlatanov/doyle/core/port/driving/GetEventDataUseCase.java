package xyz.zlatanov.doyle.core.port.driving;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetEventDataUseCase {

    Map<String, List<Map<String, Object>>> get(UUID eventId);
}
