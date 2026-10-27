package xyz.zlatanov.doyle.adapter.driving;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.port.driving.GetEventDataUseCase;
import xyz.zlatanov.doyle.core.port.driving.SeedBetsUseCase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "POC", description = """
        Proof-of-concept helpers for seeding test bets and inspecting the stored data of events, not part of the \
        settlement flow.""")
@RestController
@RequestMapping("/poc")
@RequiredArgsConstructor
public class PocRestController {

    private final SeedBetsUseCase     seedBetsUseCase;
    private final GetEventDataUseCase getEventDataUseCase;

    @PostMapping("/bets")
    @ResponseStatus(CREATED)
    public List<UUID> seedBets(@Valid @RequestBody SeedBetsRequest request) {
        return seedBetsUseCase.seed(request.toCommand()).stream()
                .map(BetId::value)
                .toList();
    }

    @GetMapping("/event-data")
    public Map<String, List<Map<String, Object>>> getEventData(@RequestParam(required = false) UUID eventId) {
        return getEventDataUseCase.get(eventId == null ? null : new EventId(eventId));
    }
}
