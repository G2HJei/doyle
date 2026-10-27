# Doyle

_Named after the legendary poker player Doyle Brunson (1933-2023)._

## Running The App

> Requires Java 17 (or later), Maven and Docker.

1. `docker compose up -d` deploys the Kafka and RocketMQ infrastructure.
2. `./mvnw spring-boot:run` runs Doyle

## Doyle REST API

Once Doyle is running, its REST API will be accessible
at [localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

## Using Doyle

Bets are placed outside Doyle, so the `/poc` endpoints seed test bets and inspect stored data. Data lives in an
in-memory H2 database and is lost on restart. The same calls can be made from the Swagger UI.

1. Seed bets on an event. All `createCount` bets share the template; seed again with another `eventWinnerId` for
   losing bets. Returns the created bet ids.
   ```sh
   curl -X POST localhost:8080/poc/bets -H 'Content-Type: application/json' -d '{
     "template": {
       "userId":        "00000000-0000-0000-0000-000000000001",
       "eventId":       "00000000-0000-0000-0000-0000000000e1",
       "eventMarketId": "00000000-0000-0000-0000-0000000000a1",
       "eventWinnerId": "00000000-0000-0000-0000-0000000000c1",
       "amount":        10.00
     },
     "createCount": 3
   }'
   ```
2. Publish the event outcome. Answers `202 Accepted`; bets are settled asynchronously within seconds.
   ```sh
   curl -X POST localhost:8080/event-outcomes -H 'Content-Type: application/json' -d '{
     "eventId":       "00000000-0000-0000-0000-0000000000e1",
     "eventName":     "Real Madrid CF vs FC Barcelona",
     "eventWinnerId": "00000000-0000-0000-0000-0000000000c1"
   }'
   ```
3. Inspect the event's `event_outcome`, `bet`, `bet_settlement` and `settlement_job` rows. Omit `eventId` to list all
   events.
   ```sh
   curl 'localhost:8080/poc/event-data?eventId=00000000-0000-0000-0000-0000000000e1'
   ```
   Bets on the winner are `SETTLED`/`WON` with a `bet_settlement` paying out their amount. The rest are
   `SETTLED`/`LOST` with no payout.
4. To correct the winner, publish the outcome again with a different `eventWinnerId`. The outcome version increments
   and the bets are resettled: revoked wins get a negative payout, new wins a positive one.

## Testing

- `mvn test` runs the unit tests.
- `mvn test -Dtest=BetSettlementAcceptanceTest` runs the acceptance tests.

## Assumptions & Decisions

- Assumed the market is always based on the winner of the event.
- Assumed the odds are always 2.00 meaning the bet amount is the sum to be settled


- Bet settlement always uses latest outcome data for the event to settle against.
- Split the publish outcome use case to track event outcome versions in case of winner correction at a later point
  (error, VAR decision etc.) resulting in a second outbox

## Architecture overview

```
+--------------------+                                                  +--------------------------+
|  DRIVING ADAPTERS  |                 ________________                 |  DRIVEN ADAPTERS         |
|                    |                /                \                |                          |
|  REST controllers  | port.driving  /       core       \  port.driven  |  JDBC repositories (H2)  |
|  Kafka listener    |------------->/                    \------------->|  Kafka publisher         |
|  RocketMQ listener |              \ use cases + domain /              |  RocketMQ publisher      |
|  Scheduled jobs    |               \                  /               |                          |
+--------------------+                \________________/                +--------------------------+
```

### Business flow

```
+-----------------------------+
|   Event outcome received    |   POST /event-outcomes
+-----------------------------+
               |
               v
+-----------------------------+
|       Outcome stored        |   new version on correction
+-----------------------------+
               |
               |  Kafka: event-outcomes
               v
+-----------------------------+
|   Settlement job created    |
+-----------------------------+
               |
               v
+-----------------------------+
| Bets for the event matched  |   in chunks
+-----------------------------+
               |
               |  RocketMQ: bet-settlements (one message per bet)
               v
+-----------------------------+
|   Bet settled WON / LOST    |   payout delta recorded
+-----------------------------+
```

Event outcome REST API stores the event outcome optimistically in an outbox, answering `409 Conflict` in case of failure
to acquire optimistic lock. Errors are RFC 9457 problem details, invalid fields listed in `errors`.

Event outcome relay polls for unpublished event outcomes every second. Pessimistically locks outcomes and publishes to
`event-outcomes` with `event id` _partition key_ and `event id` + `outcome version` _idempotency key_.

Event outcome message consumer dedupes messages on database level using the idempotency key data. A repeated message is
a no-op. Then creates `settlement job` for chunked matching of bets to settle.

The settlement job uses pessimistic lock to allow scaling. It forms an outbox of matched bets for settling sent to
`bet-settlements`. It also leverages UIUD v7 ordering to track the end of the current chunk.

Bet settlements are _partitioned_ by `user id` so that a user's bets is always settled in order, including settlement
corrections. Redelivery of older settlement messages are no-op.
