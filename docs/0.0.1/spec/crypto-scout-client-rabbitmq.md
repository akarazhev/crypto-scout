# Crypto Scout Client RabbitMQ Integration Specification

## Introduction

This document specifies the RabbitMQ integration for the `crypto-scout-client` service, detailing the exchanges, queues,
and their configurations.

## Architecture

The RabbitMQ integration follows a topic-based messaging architecture with the following components:

### Exchanges

| Exchange Name      | Type  | Durability | Description                           |
|--------------------|-------|------------|---------------------------------------|
| `metrics-exchange` | Topic | Durable    | Exchange for metrics-related messages |
| `crypto-exchange`  | Topic | Durable    | Exchange for cryptocurrency data      |
| `client-exchange`  | Topic | Durable    | Exchange for client-related messages  |

### Queues

| Queue Name                  | Bound to Exchange  | Routing Key     | Dead Letter                 | TTL (ms) | Max Length |
|-----------------------------|--------------------|-----------------|-----------------------------|----------|------------|
| `metrics-cmc-queue`         | `metrics-exchange` | `metrics.cmc`   | `metrics-dead-letter-queue` | 21600000 | 2500       |
| `metrics-bybit-queue`       | `metrics-exchange` | `metrics.bybit` | `metrics-dead-letter-queue` | 21600000 | 2500       |
| `metrics-dead-letter-queue` | -                  | -               | -                           | -        | -          |
| `crypto-bybit-stream`       | `crypto-exchange`  | `crypto.bybit`  | -                           | -        | -          |
| `crypto-scout-client-queue` | `client-exchange`  | `client`        | -                           | -        | -          |

## Queue Types

### Stream Queue

The following queues are configured as RabbitMQ streams for high-throughput, real-time data processing:

| Queue Name                 | Property                 | Purpose                                            |
|----------------------------|--------------------------|----------------------------------------------------|
| crypto-bybit-stream        | amqp.stream.crypto_bybit | Real-time cryptocurrency data from Bybit websocket |

Stream queue is configured with:
- Queue type: stream (`x-queue-type: stream`)
- Maximum length in bytes: 2GB (`x-max-length-bytes: 2_000_000_000`)
- Maximum segment size: 100MB (`x-stream-max-segment-size-bytes: 100_000_000`)

### Traditional Queues

The following queues are configured as traditional RabbitMQ queues with TTL and max length:

| Queue Name                | Property         | Purpose                               |
|---------------------------|------------------|---------------------------------------|
| metrics-cmc-queue         | amqp.queue.cmc   | CoinMarketCap Fear & Greed Index data |
| metrics-bybit-queue       | amqp.queue.bybit | Bybit Launchpool data                 |
| metrics-dead-letter-queue | amqp.queue.dead  | Dead letter queue for failed messages |

Traditional queues are configured with:
- TTL: 21,600,000 ms (6 hours)
- Max length: 2,500 messages
- Dead letter exchange configuration

## Configuration Details

### Constants

The following constants are defined in `Constants.AMQP`:

```java
// Routing keys
public static final String ROUTING_KEY_METRICS_CMC = "metrics.cmc";
public static final String ROUTING_KEY_METRICS_BYBIT = "metrics.bybit";
public static final String ROUTING_KEY_CRYPTO_BYBIT = "crypto.bybit";
public static final String ROUTING_KEY_CLIENT = "client";

// Dead letter configuration
public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
public static final String X_DEAD_LETTER_EXCHANGE_VALUE = "";
```

### Application Properties

The RabbitMQ configuration in `application.properties`:

```properties
# RabbitMQ Connection settings
amqp.exchange.metrics=metrics-exchange
amqp.exchange.crypto=crypto-exchange
amqp.exchange.client=client-exchange
amqp.queue.cmc=metrics-cmc-queue
amqp.queue.bybit=metrics-bybit-queue
amqp.queue.dead=metrics-dead-letter-queue
amqp.queue.client=crypto-scout-client-queue
amqp.stream.bybit=crypto-bybit-stream
amqp.queue.ttl.ms=21600000
amqp.queue.max.length=2500
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT:5672}
spring.rabbitmq.username=
spring.rabbitmq.password=
```

## Message Flow

1. **Metrics Data Flow**:
    - Messages published to `metrics-exchange` with routing key `metrics.cmc` are delivered to
      `metrics-cmc-queue`
    - Messages published to `metrics-exchange` with routing key `metrics.bybit` are delivered to
      `metrics-bybit-queue`
    - Failed message processing results in messages being sent to `metrics-dead-letter-queue`

2. **Cryptocurrency Data Flow**:
    - Messages published to `crypto-exchange` with routing key `crypto.bybit` are delivered to `crypto-bybit-stream` as a
      stream

3. **Client Data Flow**:
    - Messages published to `client-exchange` with routing key `client` are delivered to `crypto-scout-client-queue` as
      a queue

## Error Handling

1. **Dead Letter Queues**:
    - `metrics-dead-letter-queue`: Collects failed messages from `metrics-cmc-queue` and `metrics-bybit-queue`

2. **Message TTL**:
    - All queues have a TTL of 21,600,000 ms (6 hours)

3. **Queue Size Limits**:
    - All queues have a maximum length of 2,500 messages

## Security

RabbitMQ connection uses the following credentials:

- Username: 
- Password: 
- Host: Configurable via environment variable `SPRING_RABBITMQ_HOST` (default: localhost)
- Port: Configurable via environment variable `SPRING_RABBITMQ_PORT` (default: 5672)
