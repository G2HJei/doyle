package xyz.zlatanov.doyle.acceptance.driver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Protocol driver of the acceptance tests: translates DSL instructions to calls of Doyle's public REST API. Speaks the
 * API's JSON with its own records instead of reusing production classes, so a breaking API change fails the tests.
 */
public class DoyleRestDriver {

    private final RestClient restClient;

    public DoyleRestDriver(String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<UUID> seedBets(UUID userId, UUID eventId, UUID eventMarketId, UUID eventWinnerId, BigDecimal amount,
                               int count) {
        return restClient.post()
                .uri("/poc/bets")
                .body(new SeedBetsRequest(new BetTemplate(userId, eventId, eventMarketId, eventWinnerId, amount), count))
                .retrieve()
                .body(new ParameterizedTypeReference<List<UUID>>() {
                });
    }

    public void publishEventOutcome(UUID eventId, String eventName, UUID eventWinnerId) {
        restClient.post()
                .uri("/event-outcomes")
                .body(new EventOutcomeRequest(eventId, eventName, eventWinnerId))
                .retrieve()
                .toBodilessEntity();
    }

    public EventData getEventData(UUID eventId) {
        return restClient.get()
                .uri("/poc/event-data?eventId={eventId}", eventId)
                .retrieve()
                .body(EventData.class);
    }

    record SeedBetsRequest(BetTemplate template, int createCount) {
    }

    record BetTemplate(UUID userId, UUID eventId, UUID eventMarketId, UUID eventWinnerId, BigDecimal amount) {
    }

    record EventOutcomeRequest(UUID eventId, String eventName, UUID eventWinnerId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventData(@JsonProperty("bet") List<BetRow> bets,
                            @JsonProperty("bet_settlement") List<BetSettlementRow> betSettlements) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BetRow(UUID id, String status, String result) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BetSettlementRow(UUID betId, BigDecimal deltaAmount, int settledEventVersion) {
    }
}
