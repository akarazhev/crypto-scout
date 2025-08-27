# Crypto Scout Client RabbitMQ Integration Report

## Overview

This report documents the implementation of RabbitMQ integration for the `crypto-scout-client` service. The integration
follows best practices and design patterns for message-oriented middleware in Spring Boot applications.

## Implementation Details

### 1. Constants Definition

Added new constants in `Constants.AMQP` class to define routing keys:

```java
// Routing keys
public static final String ROUTING_KEY_METRICS_CMC_FGI = "metrics.cmc_fgi";
public static final String ROUTING_KEY_METRICS_BYBIT_LPL = "metrics.bybit_lpl";
public static final String ROUTING_KEY_CRYPTO_BYBIT = "crypto.bybit";
public static final String ROUTING_KEY_CLIENT = "client";
```

### 2. Application Properties Configuration

Updated `application.properties` to include all required RabbitMQ exchange and queue configurations:

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
```

### 3. AMQP Configuration

Enhanced `AmqpConfig.java` to define:

1. **Exchanges**:
    - `metrics-exchange`: Topic exchange for metrics data
    - `crypto-exchange`: Topic exchange for cryptocurrency data
    - `client-exchange`: Topic exchange for client-related messages

2. **Queues**:
    - `metrics-cmc-fgi-queue`: For CoinMarketCap Fear & Greed Index metrics
    - `metrics-bybit-lpl-queue`: For Bybit Launch Pool metrics
    - `metrics-dead-letter-queue`: Dead letter queue for metrics exchange
    - `crypto-bybit-queue`: Stream for Bybit cryptocurrency data
    - `crypto-scout-client-queue`: Stream for client data

3. **Bindings**:
    - Connected queues to their respective exchanges with appropriate routing keys

4. **Message Conversion**:
    - Configured Jackson JSON message converter for serialization/deserialization

## Design Patterns and Best Practices

1. **Dependency Injection**: Used Spring's dependency injection for configuration beans
2. **Builder Pattern**: Used QueueBuilder and ExchangeBuilder for clean, fluent configuration
3. **Separation of Concerns**: Kept configuration separate from business logic
4. **Externalized Configuration**: Used properties file for configurable values
5. **Dead Letter Queues**: Implemented for handling failed message processing
6. **Durable Messaging**: Configured exchanges and queues as durable for message persistence
7. **TTL and Max Length**: Set appropriate time-to-live and maximum length for queues

## Conclusion

The RabbitMQ integration for the crypto-scout-client service has been successfully implemented following Spring Boot
best practices. The configuration supports the required exchanges and queues as specified, with proper error handling
through dead letter queues.
