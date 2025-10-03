# Crypto Scout Collector: ActiveJ 6.0-rc2 Reactive JDBC Integration

## Summary

- Replaced Spring Data JPA-based persistence with reactive, non-blocking orchestration using ActiveJ + plain JDBC.
- Implemented `com.github.akarazhev.cryptoscout.collector.BybitService` and `...collector.CmcService` as ActiveJ `ReactiveService`s.
- Centralized DB setup in `com.github.akarazhev.cryptoscout.config.JdbcConfig` using `PGSimpleDataSource` and `AppConfig`.
- Ensured AMQP ack happens only after DB write completes in `com.github.akarazhev.cryptoscout.collector.AmqpConsumer`.

The ActiveJ launcher (`com.github.akarazhev.cryptoscout.Collector`) and DI wiring (`com.github.akarazhev.cryptoscout.module.CollectorModule`, `CoreModule`) remain the same.

## What changed

- __`crypto-scout-collector/src/main/java/com/github/akarazhev/cryptoscout/collector/BybitService.java`__
  - Now buffers two kinds of data parsed from Bybit payloads: spot tickers (BTC/USDT, ETH/USDT) and Launch Pool (LPL).
  - Uses `ConcurrentLinkedQueue` buffers and an ActiveJ reactor task scheduled with `delayBackground()` to periodically flush.
  - Inserts in batches using plain JDBC into:
    - `crypto_scout.bybit_spot_tickers_btc_usdt`
    - `crypto_scout.bybit_spot_tickers_eth_usdt`
    - `crypto_scout.bybit_lpl`
  - CPU-bound or blocking DB I/O is executed off-reactor via `Promise.ofBlocking(executor, ...)` (virtual threads).
  - Batch size and flush interval read from `application.properties` using `AppConfig` keys:
    - `crypto-scout.bybit.batch-size`
    - `crypto-scout.bybit.flush-interval-ms`

- __`crypto-scout-collector/src/main/java/com/github/akarazhev/cryptoscout/collector/CmcService.java`__
  - Parses CMC Fear & Greed Index (`Source.FGI`) list from payload and performs a single batch insert into `crypto_scout.cmc_fgi`.
  - All blocking DB work is delegated to the executor via `Promise.ofBlocking`.

- __`crypto-scout-collector/src/main/java/com/github/akarazhev/cryptoscout/config/JdbcConfig.java`__
  - Provides `createDataSource()` backed by `org.postgresql.ds.PGSimpleDataSource`.
  - Reads config from `application.properties` (through `AppConfig`):
    - `spring.datasource.url`
    - `spring.datasource.username`
    - `spring.datasource.password`

- __`crypto-scout-collector/src/main/java/com/github/akarazhev/cryptoscout/collector/AmqpConsumer.java`__
  - Now chains `service.save(...)` promise and acknowledges message only on successful completion; nacks on failure.
  - Still keeps AMQP backpressure with `basicQos(64)`.

Note: Legacy Spring `@Service` implementations remain in the tree (e.g., `bybit/BybitServiceImpl.java`, `cmc/CmcServiceImpl.java`) but are not used by ActiveJ runtime. The ActiveJ graph wires `collector.BybitService` and `collector.CmcService` via `CollectorModule`.

## Design and best practices

- __Reactor-friendly orchestration__: All coordination is single-threaded on the `NioReactor`. Blocking JDBC is offloaded to virtual threads via `Promise.ofBlocking`.
- __Batching & buffering__: Reduces DB round trips. Snapshots are taken from the queues before performing inserts to minimize contention.
- __Periodic background flush__: Implemented with `getReactor().delayBackground(interval, runnable)`; re-schedules itself to run again.
- __Ack-after-commit__: AMQP messages are acked only after DB operations finish successfully.
- __Schema alignment__: Column mapping exactly matches `podman-dev/postgres/init.sql` and `podman-prod/postgres/init.sql`.
- __Config centralization__: JDBC config is centralized and reuses the existing `application.properties` keys.

## Table mappings

Refer to `podman-dev/postgres/init.sql` for exact schemas. These are the insert statements used at runtime:

- Spot tickers (BTC/USDT or ETH/USDT):
```sql
INSERT INTO crypto_scout.bybit_spot_tickers_XXX_usdt
(timestamp, cross_sequence, last_price, high_price_24h, low_price_24h, prev_price_24h,
 volume_24h, turnover_24h, price_24h_pcnt, usd_index_price)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```
- Launch Pool (LPL):
```sql
INSERT INTO crypto_scout.bybit_lpl
(return_coin, return_coin_icon, description, website, whitepaper, rules,
 stake_begin_time, stake_end_time, trade_begin_time)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
```
- CMC Fear & Greed Index:
```sql
INSERT INTO crypto_scout.cmc_fgi (score, name, timestamp, btc_price, btc_volume)
VALUES (?, ?, ?, ?, ?)
```

All timestamps are written as `TIMESTAMPTZ` using `OffsetDateTime` in UTC.

## Configuration

- `server.port=8081`
- AMQP keys used by `AmqpConsumer` are defined in `application.properties` and read through `AppConfig` in `config/AmqpConfig.java`.
- JDBC is configured via:
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/crypto_scout`
  - `spring.datasource.username=...`
  - `spring.datasource.password=...`
- Bybit buffering:
  - `crypto-scout.bybit.batch-size=200`
  - `crypto-scout.bybit.flush-interval-ms=5000`

## How it works at runtime

1. `Collector` launcher builds the DI graph with `CoreModule`, `CollectorModule`, `WebModule` (if any web parts).
2. `CollectorModule` provides:
   - `BybitService` and `CmcService` (ActiveJ `ReactiveService`s)
   - `AmqpConsumer` (annotated with `@Eager`) which starts consuming queues/stream.
3. `AmqpConsumer` deserializes messages to `Payload` and forwards to respective service.
4. Services perform DB writes on virtual threads and resolve a `Promise` when done.
5. `AmqpConsumer` acks only after the `Promise` completes without error (else nacks => dead-letter queue).

## Runbook

- Ensure Postgres + TimescaleDB are up (see `podman-dev/` compose and `init.sql`).
- Ensure `application.properties` contains the correct JDBC credentials.
- Build and run the collector (from repository root):
  - Package: `mvn -DskipTests package -pl crypto-scout-collector -am`
  - Run jar: `java -jar crypto-scout-collector/target/crypto-scout-collector-0.0.1.jar`

## Future improvements

- Add metrics (batch sizes, flush durations, error counters) via JMX or logging.
- Add retry with backoff for transient DB errors.
- Make batch size and flush interval dynamically configurable (e.g., via JMX).
- Consider COPY API for very high insert throughput workloads.
