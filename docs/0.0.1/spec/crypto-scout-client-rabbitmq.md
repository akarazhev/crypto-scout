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

| Queue Name                  | Bound to Exchange  | Routing Key         | Dead Letter                 | TTL (ms) | Max Length |
|-----------------------------|--------------------|---------------------|----------------------------|----------|------------|
| `metrics-cmc-fgi-queue`     | `metrics-exchange` | `metrics.cmc_fgi`   | `metrics-dead-letter-queue` | 21600000 | 2500       |
| `metrics-bybit-lpl-queue`   | `metrics-exchange` | `metrics.bybit_lpl` | `metrics-dead-letter-queue` | 21600000 | 2500       |
| `metrics-dead-letter-queue` | -                  | -                   | -                          | -        | -          |
| `crypto-bybit-queue`        | `crypto-exchange`  | `crypto.bybit`      | -                          | 21600000 | 2500       |
| `crypto-scout-client-queue` | `client-exchange`  | `client`            | -                          | 21600000 | 2500       |

## Configuration Details

### Constants

The following constants are defined in `Constants.AMQP`:

```java
// Exchange names
public static final String METRICS_EXCHANGE = "metrics-exchange";
public static final String CRYPTO_EXCHANGE = "crypto-exchange";
public static final String CLIENT_EXCHANGE = "client-exchange";

// Queue names
public static final String METRICS_CMC_FGI_QUEUE = "metrics-cmc-fgi-queue";
public static final String METRICS_BYBIT_LPL_QUEUE = "metrics-bybit-lpl-queue";
public static final String METRICS_DEAD_LETTER_QUEUE = "metrics-dead-letter-queue";
public static final String CRYPTO_BYBIT_QUEUE = "crypto-bybit-queue";
public static final String CLIENT_QUEUE = "crypto-scout-client-queue";

// Routing keys
public static final String ROUTING_METRICS_CMC_FGI = "metrics.cmc_fgi";
public static final String ROUTING_METRICS_BYBIT_LPL = "metrics.bybit_lpl";
public static final String ROUTING_CRYPTO_BYBIT = "crypto.bybit";
public static final String ROUTING_CLIENT = "client";
```

### Application Properties

The RabbitMQ configuration in `application.properties`:

```properties
# RabbitMQ Connection settings
amqp.exchange.metrics=metrics-exchange
amqp.exchange.crypto=crypto-exchange
amqp.exchange.client=client-exchange
amqp.queue.cmc_fgi=metrics-cmc-fgi-queue
amqp.queue.bybit_lpl=metrics-bybit-lpl-queue
amqp.queue.dead=metrics-dead-letter-queue
amqp.queue.client=crypto-scout-client-queue
amqp.queue.crypto_bybit=crypto-bybit-queue
amqp.queue.ttl.ms=21600000
amqp.queue.max.length=2500
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT:5672}
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
```

## Message Flow

1. **Metrics Data Flow**:
    - Messages published to `metrics-exchange` with routing key `metrics.cmc_fgi` are delivered to
      `metrics-cmc-fgi-queue`
    - Messages published to `metrics-exchange` with routing key `metrics.bybit_lpl` are delivered to
      `metrics-bybit-lpl-queue`
    - Failed message processing results in messages being sent to `metrics-dead-letter-queue`

2. **Cryptocurrency Data Flow**:
    - Messages published to `crypto-exchange` with routing key `crypto.bybit` are delivered to `crypto-bybit-queue` as a
      stream

3. **Client Data Flow**:
    - Messages published to `client-exchange` with routing key `client` are delivered to `crypto-scout-client-queue` as
      a stream

## Error Handling

1. **Dead Letter Queues**:
    - `metrics-dead-letter-queue`: Collects failed messages from `metrics-cmc-fgi-queue` and `metrics-bybit-lpl-queue`

2. **Message TTL**:
    - All queues have a TTL of 21,600,000 ms (6 hours)

3. **Queue Size Limits**:
    - All queues have a maximum length of 2,500 messages

## Security

RabbitMQ connection uses the following credentials:

- Username: admin
- Password: admin
- Host: Configurable via environment variable `SPRING_RABBITMQ_HOST` (default: localhost)
- Port: Configurable via environment variable `SPRING_RABBITMQ_PORT` (default: 5672)
