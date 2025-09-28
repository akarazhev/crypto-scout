# Crypto Scout Client – ActiveJ 6.0-rc2 Reactive RabbitMQ Integration

## Overview
This document describes how the `crypto-scout-client` service replaced its Spring Boot-based RabbitMQ integration with a fully reactive implementation using ActiveJ 6.0-rc2 and the RabbitMQ Java client.

Key outcomes:
- Spring AMQP (`spring-boot-starter-amqp`) removed from `crypto-scout-client`.
- New reactive `AmqpClient` implemented with ActiveJ lifecycle and RabbitMQ Java client.
- Spring `AmqpConfig` replaced with a lightweight configuration loader (`AmqpConfig.AmqpSettings`).
- Existing consumers (`BybitConsumer`, `CmcConsumer`) now publish payloads via the new `Publisher` binding.
- DI, lifecycle, and startup are handled by ActiveJ modules (`ConfigModule`, `WebModule`, `BybitModule`, `CmcModule`) under `CryptoScoutClient` launcher.

## Architecture
- `com.github.akarazhev.cryptoscout.client.Publisher`
  - Implements `io.activej.async.service.ReactiveService` and `Publisher<Payload<Map<String, Object>>>`.
  - Manages RabbitMQ connection/channel lifecycle (connect on `start()`, close on `stop()`).
  - Declares exchanges, queues, and bindings at startup.
  - Publishes messages asynchronously (offloaded to an `Executor`) and uses publisher confirms for reliability.

- `com.github.akarazhev.cryptoscout.config.AmqpConfig`
  - Replaced Spring configuration with a plain ActiveJ-friendly loader.
  - Loads properties from `src/main/resources/application.properties` and environment variables (supports `${ENV:default}` format).
  - Exposes immutable `AmqpSettings` record consumed by DI providers.

- `com.github.akarazhev.cryptoscout.module.ClientModule`
  - Provides `ServerConfig` (unchanged), `AmqpSettings`, and an eager `AmqpClient` instance.
  - Binds `Publisher<Payload<Map<String,Object>>>` to the `AmqpClient`.

- `com.github.akarazhev.cryptoscout.module.WebModule`
  - Provides HTTP server and DNS/HTTP clients.
  - Provides eager `BybitConsumer` and `CmcConsumer` and injects `Publisher` so both can publish downstream payloads.

- `com.github.akarazhev.cryptoscout.client.BybitConsumer` and `CmcConsumer`
  - Stream and parse crypto/metrics data via ActiveJ datastreams.
  - Publish each `Payload<Map<String,Object>>` to RabbitMQ through the injected `Publisher` (AmqpClient).

- `com.github.akarazhev.cryptoscout.amqp.DataPublisher`
  - Deprecated no-op preserved for binary compatibility (Spring AMQP removed).

## Implementation Details

### 1) Reactive AMQP Client
File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/amqp/AmqpClient.java`

Highlights:
- Uses `com.rabbitmq.client.ConnectionFactory` to create a connection and channel on `start()`.
- Enables publisher confirms (`channel.confirmSelect()` + `waitForConfirmsOrDie`) for at-least-once semantics.
- Declares durable topic exchanges and queues (including a stream queue) and binds them to routing keys from `Constants.AMQP`.
- Publishes JSON payloads using Jackson (`ObjectMapper`).
- Offloads blocking operations to a virtual-thread-backed `Executor` from `CoreModule` to keep the reactor non-blocking.

Routing logic mirrors previous Spring-based `DataPublisher`:
- CMC FGI → `metrics-exchange` / `metrics.cmc`
- Bybit LPL → `metrics-exchange` / `metrics.bybit`
- Bybit PMST → `crypto-exchange` / `crypto.bybit`

### 2) Configuration Loader
File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/config/AmqpConfig.java`

- `AmqpSettings` record aggregates:
  - Connection: host, port, username, password
  - Exchanges: metrics, crypto, client, collector
  - Queues: cmc, bybit, dead-letter, client, collector
  - Stream: bybit stream name and byte retention settings
  - Queue TTL and max-length (message count)
- `loadFromClasspath()` reads `application.properties` and resolves `${ENV:default}` templates with environment variables (e.g. `AMQP_RABBITMQ_HOST`).
- Reasonable defaults used if properties are absent.

### 3) DI and Lifecycle Wiring
File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/module/ConfigModule.java`

- Provides `AmqpSettings` and an `@Eager` `AmqpClient` instance.
- Binds `Publisher<Payload<Map<String,Object>>>` to the `AmqpClient` so any component can publish without RabbitMQ coupling.

File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/module/WebModule.java`

- `BybitConsumer` and `CmcConsumer` providers now accept `Publisher<Payload<Map<String,Object>>>` and pass it on.
- Both consumers publish incoming stream payloads to RabbitMQ.

### 4) Consumers Updated to Publish
Files:
- `consumer/BybitConsumer.java`
- `consumer/CmcConsumer.java`

Both now inject and use `Publisher<Payload<Map<String,Object>>>` to send messages to RabbitMQ.

### 5) Deprecate Old Spring Publisher
File: `amqp/DataPublisher.java`

- Marked `@Deprecated` and converted to a no-op (no Spring imports) to avoid compile errors and preserve binary compatibility.

### 6) Maven Dependencies
File: `crypto-scout-client/pom.xml`

- Removed Spring AMQP starter and Boot plugin from the module.
- Added ActiveJ dependencies required by client modules:
  - `activej-http`, `activej-dns`, `activej-eventloop`, `activej-reactor`, `activej-async`, `activej-datastream`, `activej-launcher`, `activej-inject`
- Added RabbitMQ Java client: `com.rabbitmq:amqp-client`
- Added Jackson: `com.fasterxml.jackson.core:jackson-databind`
- Added SLF4J backend: `ch.qos.logback:logback-classic`
- Compiler set to Java 21.

Note: The module still uses the Spring Boot parent POM for dependency management; Spring dependencies are not used in this module anymore. This is acceptable, but we can remove the parent in a separate cleanup if desired.

## Configuration
File: `crypto-scout-client/src/main/resources/application.properties`

Used keys:
- Exchanges: `amqp.exchange.metrics`, `amqp.exchange.crypto`, `amqp.exchange.collector`
- Queues: `amqp.queue.cmc`, `amqp.queue.bybit`, `amqp.queue.dead`, `amqp.queue.client`, `amqp.queue.collector`
- Stream: `amqp.stream.bybit`
- Queue params: `amqp.queue.ttl.ms`, `amqp.queue.max.length`
- Optional stream params (with defaults if omitted): `amqp.stream.max.bytes`, `amqp.stream.segment.bytes`
- Connection (supports env templating): `amqp.rabbitmq.host`, `amqp.rabbitmq.port`, `amqp.rabbitmq.username`, `amqp.rabbitmq.password`

Environment variables honored via `${ENV:default}` syntax:
- `AMQP_RABBITMQ_HOST`, `AMQP_RABBITMQ_PORT`, `AMQP_RABBITMQ_USERNAME`, `AMQP_RABBITMQ_PASSWORD`

## Topology Declared at Startup
- Topic exchanges (durable): metrics, crypto, client, collector
- Classic queues (durable) with TTL and max-length and dead-letter routing: cmc, bybit, client, collector
- Dead-letter queue (durable): for metrics
- Stream queue (durable): `crypto-bybit-stream` with byte retention and segment size
- Bindings:
  - `metrics.cmc` → cmc queue
  - `metrics.bybit` → bybit queue
  - `crypto.bybit` → stream queue
  - `client` → client queue
  - `collector` → collector queue

## Design Patterns and Best Practices
- Dependency Inversion: components depend on `Publisher<T>` rather than RabbitMQ specifics.
- Separation of Concerns: configuration loader, publisher, and consumers have clear boundaries.
- Reactive Lifecycle: `ReactiveService` start/stop used for non-blocking orchestration.
- Backpressure-friendly: ActiveJ datastreams process and publish events asynchronously.
- Reliability: publisher confirms enabled for at-least-once delivery.
- Externalized Configuration: property and environment-based configuration with sensible defaults.
- Stream vs Classic Queues: stream queue used for high-throughput market data; TTL + max-length used for classic queues.

## Migration Notes
- Spring AMQP beans and `DataPublisher` usage have been replaced with ActiveJ DI and `AmqpClient` publisher.
- Any direct injection of `DataPublisher` should be replaced with `Publisher<Payload<Map<String,Object>>>` (now provided by DI and backed by `AmqpClient`).
- The deprecated `DataPublisher` remains to avoid breakages but no longer sends messages.

## How to Build and Run
- Ensure RabbitMQ is accessible. Default host/port/user/password come from environment (or defaults in properties). 
- Build from the repo root to assemble all modules:
  - `mvn -DskipTests package -pl crypto-scout-client -am`
- Run `CryptoScoutClient` main class. The ActiveJ launcher will initialize modules and start services, including the AMQP client and consumers.

## Verification Checklist
- AmqpClient starts and logs topology declaration.
- Bybit and CMC consumers log incoming payloads and publish them to the correct routes.
- Messages appear in the corresponding RabbitMQ queues/stream.
- Graceful shutdown closes channel and connection without errors.

## Future Enhancements
- Add retry/backoff on connection loss with topology re-declaration.
- Add consumer-side services to process messages from queues.
- Add metrics/JMX exposition for AMQP publishing rates and connection health.
