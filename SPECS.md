# Kafka & RocketMQ

## Requirements

Your task is to write a backend application that will simulate sports betting event outcome handling and bet settlement via Kafka and RocketMQ.

- **An API endpoint to publish a sports event outcome to Kafka.**
- **A Kafka consumer that listens to `event-outcomes` Kafka named-topic.**
- **Matches the event outcome to bets that need to be settled.**
- **A RocketMQ producer that sends messages to `bet-settlements`.**
- **A RocketMQ Consumer that listens to `bet-settlements`.**

The details of these Use Cases can be found below.

---

## Sports Betting Settlement Trigger Service

> We want to launch a new backend service that will simulate sports betting event outcome handling and bet settlement via Kafka and RocketMQ. Here you have the relevant Use Cases to support:

1. **An API endpoint to publish a sports event outcome to Kafka.**
    1. This endpoint should publish an event outcome to Kafka. An event outcome is represented by:
        1. Event ID
        2. Event Name
        3. Event Winner ID
    2. The topic should be `event-outcomes` as commented before.

2. **A Kafka consumer that listens to `event-outcomes` Kafka named-topic.**

3. **Matches the event outcome to bets that need to be settled.**
    1. The System checks if we have bets in our database that can be settled based on Event ID from the event outcome message.
    2. A bet in database is composed by:
        1. Bet ID
        2. User ID
        3. Event ID
        4. Event Market ID
        5. Event Winner ID
        6. Bet Amount

4. **A RocketMQ producer that sends messages to `bet-settlements`.**
    1. After identifying the bets to be settled in the previous point, here we should produce the messages for RocketMQ so the bet can be settled.

5. **A RocketMQ consumer that listens to `bet-settlements`.**
    1. Here we should consume the messages and settle the bets.

### Conditions

- Use an in-memory database for the bets.