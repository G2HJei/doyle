package xyz.zlatanov.doyle.core;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.port.driven.EventDataRepository;
import xyz.zlatanov.doyle.core.port.driving.GetEventDataUseCase;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class GetEventDataCommandProcessor implements GetEventDataUseCase {

    private final EventDataRepository eventDataRepository;

    @Transactional(readOnly = true)
    @Override
    public Map<String, List<Map<String, Object>>> get(EventId eventId) {
        return eventDataRepository.findByEventId(eventId);
    }
}
