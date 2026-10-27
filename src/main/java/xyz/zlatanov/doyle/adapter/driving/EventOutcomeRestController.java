package xyz.zlatanov.doyle.adapter.driving;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

import static org.springframework.http.HttpStatus.ACCEPTED;

@Tag(name = "Event outcomes", description = """
        Proof-of-concept helpers for seeding test bets and inspecting the stored data of events, not part of the \
        settlement flow.""")
@RestController
@RequestMapping("/event-outcomes")
@RequiredArgsConstructor
public class EventOutcomeRestController {

    private final PublishEventOutcomeUseCase publishEventOutcomeUseCase;

    @PostMapping
    @ResponseStatus(ACCEPTED)
    public void publish(@Valid @RequestBody PublishEventOutcomeCommand command) {
        publishEventOutcomeUseCase.publish(command);
    }
}
