# Crypto Scout Collector — ActiveJ Reactive RabbitMQ Integration

## Summary

- __Goal__: Replace Spring AMQP subscribers (`BybitQueueSubscriber`, `BybitStreamSubscriber`, `CmcQueueSubscriber`) with an ActiveJ 6.0-rc2 reactive consumer.
- __Result__: Implemented `AmqpConsumer` in `crypto-scout-collector` and wired it via `CollectorModule`. The consumer connects to RabbitMQ, consumes from CMC, Bybit, and Bybit Stream queues, and dispatches to `CmcService` and `BybitService` respectively.

## Key Changes

- __`collector/AmqpConsumer`__: Reactive service managing RabbitMQ connection, channel, and consumers.
  - Uses `Promise.ofBlocking(executor, ...)` to offload blocking AMQP calls to a virtual-thread executor while keeping the reactor thread non-blocking.
  - Declares/ensures queues exist (idempotent) using names from `collector/config/AmqpConfig`.
  - Subscribes to:
    - `AmqpConfig.getAmqpQueueCmc()` → dispatch to `CmcService.save(...)`.
    - `AmqpConfig.getAmqpQueueBybit()` → dispatch to `BybitService.save(...)`.
    - `AmqpConfig.getAmqpStreamBybit()` → dispatch to `BybitService.save(...)`.
  - Acknowledges on success, `basicNack` (no requeue) on failure to leverage DLX routing configured by the publisher topology.
  - Applies `basicQos(64)` for consumer-side backpressure.
- __`module/CollectorModule`__: Provides `AmqpConsumer` eagerly with DI deps: `NioReactor`, `Executor`, `BybitService`, `CmcService`.

## Design Notes & Best Practices

- __Reactive lifecycle__: `AmqpConsumer` implements `ReactiveService` with non-blocking start/stop that offloads networking/blocking work to `Executor` while preserving the single-threaded `NioReactor` semantics.
- __At-least-once delivery__: Explicit `basicAck` on success and `basicNack(requeue=false)` on failure; failed messages are dead-lettered per topology declared by `client/AmqpPublisher`.
- __Backpressure__: `basicQos(64)` limits unacked deliveries per consumer; work is executed on a virtual-thread pool for simplicity and throughput with Java 21.
- __Idempotent declarations__: `declareQueuesIfNeeded()` is safe even if `AmqpPublisher` already declared the topology.
- __Separation of concerns__: `AmqpConsumer` focuses purely on transport and dispatch; domain logic remains in `BybitService` and `CmcService`.

## File References

- __Collector Launcher__: `crypto-scout-collector/src/main/java/com/github/akarazhev/cryptoscout/Collector.java`
- __ActiveJ Modules__:
  - `module/CoreModule.java` — provides `NioReactor` and `Executor` (virtual threads)
  - `module/CollectorModule.java` — wires `AmqpConsumer`
- __Consumer__: `collector/AmqpConsumer.java`
- __Config__: `collector/config/AmqpConfig.java` (reads from application properties via `AppConfig`)
- __Publisher (reference)__: `crypto-scout-client/.../client/AmqpPublisher.java`
- __Services__:
  - `bybit/BybitService.java`, `bybit/BybitServiceImpl.java`
  - `cmc/CmcService.java`, `cmc/CmcServiceImpl.java`

## Error Handling & Observability

- __Logging__: Start/stop lifecycle, consumer cancellations, and per-message failures are logged with context.
- __Nack policy__: On processing exception, message is negatively acknowledged without requeue, preventing tight failure loops and honoring DLX.

## Migration Notes

- The Spring `@RabbitListener` subscribers remain in the codebase but are not used by the ActiveJ runtime; no Spring `ApplicationContext` is bootstrapped by the `Collector` launcher. They can be removed in a subsequent cleanup when Spring dependencies are fully phased out.

## Next Steps (Optional)

- __Topology parity__: If needed, mirror the exact queue args used by the publisher (DLX, TTL, max length, etc.) on the consumer side declarations.
- __Metrics__: Export consumer lag, acks/nacks counters, and processing latency via JMX/metrics.
- __Retries__: Consider a retry queue/policy before DLQ for transient failures.

## Validation Checklist

- __Compile__: Module compiles with Java 21, ActiveJ 6.0-rc2.
- __Runtime__: `Collector` launches, opens AMQP connection, and starts consuming.
- __Dispatch__: Messages from CMC and Bybit queues/stream are deserialized and persisted via `CmcService` / `BybitService`.
- __Reliability__: Success → ack; failure → nack to DLX.
