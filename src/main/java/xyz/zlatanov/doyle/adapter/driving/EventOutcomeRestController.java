package xyz.zlatanov.doyle.adapter.driving;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import xyz.zlatanov.doyle.core.domain.PublishEventOutcomeCommand;
import xyz.zlatanov.doyle.core.port.driving.PublishEventOutcomeUseCase;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@Tag(name = "Event outcomes", description = """
        Publishes sports event outcomes to Kafka to settle the bets on the events. Publishing an outcome again for the \
        same event corrects it and resettles the bets on the event.""")
@RestController
@RequestMapping("/event-outcomes")
@RequiredArgsConstructor
public class EventOutcomeRestController {

    private final PublishEventOutcomeUseCase publishEventOutcomeUseCase;

    @PostMapping
    @ResponseStatus(ACCEPTED)
    @ApiResponse(responseCode = "202", description = "Outcome stored, it will be published to Kafka")
    @ApiResponse(responseCode = "400", description = "Malformed outcome or invalid fields, listed in `errors`",
            content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Outcome of the event changed by a concurrent request. Retry.",
            content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public void publish(@Valid @RequestBody PublishEventOutcomeCommand command) {
        publishEventOutcomeUseCase.publish(command);
    }
}
