package xyz.zlatanov.doyle.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class SeedBetsCommand {

    @NotNull
    @Valid
    private BetTemplate template;
    @Positive
    private int         createCount;

    @Data
    @Accessors(fluent = true)
    @Getter(onMethod = @__(@JsonProperty))
    public static class BetTemplate {

        @NotNull
        private UUID       userId;
        @NotNull
        private UUID       eventId;
        @NotNull
        private UUID       eventMarketId;
        @NotNull
        private UUID       eventWinnerId;
        @NotNull
        @Positive
        private BigDecimal amount;
    }
}
