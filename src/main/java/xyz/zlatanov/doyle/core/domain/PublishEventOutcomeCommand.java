package xyz.zlatanov.doyle.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class PublishEventOutcomeCommand {

    @NotNull
    private UUID   eventId;
    @NotBlank
    @Size(max = 255)
    private String eventName;
    @NotNull
    private UUID   eventWinnerId;
}
