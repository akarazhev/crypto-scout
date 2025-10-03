# crypto-scout-client: RabbitMQ Stream Publisher Refactor

Date: 2025-10-03
Module: `crypto-scout-client`
Key files:
- `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/client/AmqpPublisher.java`
- `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/config/AmqpConfig.java`
- `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/config/Constants.java`
- `crypto-scout-client/src/main/resources/application.properties`

## Summary
- Replaced AMQP 0-9-1 publishing for BYBIT PMST with RabbitMQ Streams using `stream-client` (v1.2.0).
- Kept AMQP 0-9-1 publishing for metrics (CMC.FGI and BYBIT.LPL) using publisher confirms.
- Added client configuration for RabbitMQ Stream host/port and stream name.
- Ensured non-blocking publishing on the ActiveJ reactor with confirm-backed completion.

## Motivation
- BYBIT PMST feed is high-throughput and append-only; RabbitMQ Streams provide higher throughput, persistence, and replay capability.
- Metrics flows remain simple topic routing via exchanges/queues.

## Implementation Details

- AmqpPublisher
  - File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/client/AmqpPublisher.java`
  - Changes:
    - Added RabbitMQ Streams `Environment` and `Producer` fields.
    - `start()` now:
      - Initializes AMQP channel and enables publisher confirms.
      - Builds Streams `Environment` from `AmqpConfig` host/stream port/credentials.
      - Idempotently creates the BYBIT stream (safe to run even if pre-provisioned).
      - Creates a Streams `Producer` bound to the configured stream.
    - `stop()` now closes producer, environment, AMQP channel, and connection.
    - `publish(...)` routes:
      - BYBIT + PMST → `publishToStream()` using `producer.send(..., confirmationStatus)` and completes a `SettablePromise` on confirm.
      - CMC.FGI and BYBIT.LPL → AMQP exchange publish with `waitForConfirmsOrDie()` for at-least-once semantics.

- AmqpConfig
  - File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/config/AmqpConfig.java`
  - Exposed getters used by stream client:
    - `getAmqpStreamBybit()`
    - `getAmqpRabbitmqStreamPort()`
    - Made host/port/username/password getters public (reused by both clients).

- Constants
  - File: `crypto-scout-client/src/main/java/com/github/akarazhev/cryptoscout/config/Constants.java`
  - Added keys for stream configuration:
    - `amqp.stream.bybit`
    - `amqp.stream.port`

- Application Properties
  - File: `crypto-scout-client/src/main/resources/application.properties`
  - Added:
    - `amqp.stream.port=5552`
    - `amqp.stream.bybit=crypto-bybit-stream`

## Design and Best Practices

- Separation of concerns: AMQP (metrics) vs Stream (BYBIT PMST). Clear routing in `publish(...)`.
- Non-blocking I/O: AMQP publishes are offloaded to the executor; Streams publish uses async confirms and completes on the reactor thread.
- Reliability:
  - AMQP path uses publisher confirms.
  - Stream path waits for confirm callback; errors propagate via `Promise`.
- Idempotent provisioning: Stream creation is attempted; failures (e.g., already exists/no permission) are logged at debug level and publishing proceeds.
- Compatibility with existing consumer:
  - `crypto-scout-collector` consumes PMST from `amqp.stream.bybit` via AMQP `basicConsume` on the stream queue (RabbitMQ 4 stream_queue feature). This refactor maintains that contract.
- Operational safety:
  - No queue/exchange declaration from the publisher for AMQP paths, avoiding PRECONDITION_FAILED (see consumer fix using `queueDeclarePassive`).

## Configuration

- Ensure RabbitMQ Streams plugin is enabled and a TCP listener is available on the configured port (default 5552).
- Properties (client):
  - `amqp.rabbitmq.host=localhost`
  - `amqp.rabbitmq.port=5672`
  - `amqp.rabbitmq.username=admin`
  - `amqp.rabbitmq.password=admin`
  - `amqp.stream.port=5552`
  - `amqp.stream.bybit=crypto-bybit-stream`
- Properties (collector) already align: `amqp.stream.bybit=crypto-bybit-stream`.
- If production stream names differ, set `amqp.stream.bybit` accordingly; publisher will target the configured stream.

## Failure Handling & Future Improvements

- Add send throttling/backpressure for Streams (e.g., limit in-flight sends) if needed under extreme load.
- Implement retry with bounded backoff for transient stream publish failures.
- Add tracing/metadata headers (e.g., messageId, creationTime) when building Stream messages for better observability.
- Surface publish metrics (success/failure rates, latency) via JMX/metrics registry.
- Consider key-based partitioning if moving to multi-partition streams in the future.

## Validation Steps

1) Build the client module:
   - `mvn -pl crypto-scout-client -am -DskipTests package`
2) Run RabbitMQ with Streams enabled (port 5552) and verify stream exists (or allow auto-create).
3) Start client and collector locally:
   - Client should publish BYBIT PMST to the stream; metrics to exchanges.
   - Collector should consume from `amqp.stream.bybit` and process BYBIT data, and from metrics queues for metrics data.
4) Verify no PRECONDITION_FAILED on consumer startup (queues declared passively).
5) Check broker confirms and confirm callbacks (logs) to ensure at-least-once semantics.

## Notes
- This change is transparent for metrics consumers; only BYBIT PMST path switches to Streams.
- The stream creation is idempotent and safe; for locked-down environments, pre-provision the stream and ensure credentials permit publishing.
