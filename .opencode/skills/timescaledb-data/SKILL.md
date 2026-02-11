---
name: timescaledb-data
description: TimescaleDB data modeling and operations for crypto market time-series data
license: MIT
compatibility: opencode
metadata:
  database: timescaledb
  version: "pg17"
  domain: time-series
---

## What I Do

Guide TimescaleDB data modeling, schema design, and operations for cryptocurrency market time-series data in the crypto-scout ecosystem.

## Database Architecture

### Schema Overview
```sql
-- Main schema: crypto_scout
crypto_scout
├── stream_offsets              -- Stream offset tracking (exactly-once)
│
├── BYBIT SPOT TABLES
├── bybit_spot_tickers          -- Spot market tickers (1m interval)
├── bybit_spot_kline_1m         -- 1-minute candlesticks
├── bybit_spot_kline_5m         -- 5-minute candlesticks
├── bybit_spot_kline_15m        -- 15-minute candlesticks
├── bybit_spot_kline_60m        -- 1-hour candlesticks
├── bybit_spot_kline_240m       -- 4-hour candlesticks
├── bybit_spot_kline_1d         -- Daily candlesticks
├── bybit_spot_public_trade     -- Public trade data
├── bybit_spot_order_book_1     -- Order book (1 level)
├── bybit_spot_order_book_50    -- Order book (50 levels)
├── bybit_spot_order_book_200   -- Order book (200 levels)
├── bybit_spot_order_book_1000  -- Order book (1000 levels)
│
├── BYBIT LINEAR TABLES
├── bybit_linear_tickers        -- Linear market tickers
├── bybit_linear_kline_1m       -- 1-minute candlesticks
├── bybit_linear_kline_5m       -- 5-minute candlesticks
├── bybit_linear_kline_15m      -- 15-minute candlesticks
├── bybit_linear_kline_60m      -- 1-hour candlesticks
├── bybit_linear_kline_240m     -- 4-hour candlesticks
├── bybit_linear_kline_1d       -- Daily candlesticks
├── bybit_linear_public_trade   -- Public trade data
├── bybit_linear_order_book_1   -- Order book (1 level)
├── bybit_linear_order_book_50  -- Order book (50 levels)
├── bybit_linear_order_book_200 -- Order book (200 levels)
├── bybit_linear_order_book_1000-- Order book (1000 levels)
├── bybit_linear_all_liquidation-- Liquidation events
│
├── CRYPTO SCOUT TABLES
├── cmc_fgi                     -- Fear & Greed Index
├── cmc_kline_1d               -- CMC BTC/USD daily klines
├── cmc_kline_1w               -- CMC BTC/USD weekly klines
│
└── ANALYST TABLES
    ├── bybit_lpl              -- Liquidation pressure level
    ├── btc_price_risk         -- BTC price risk mapping
    └── btc_risk_price         -- Current risk assessment
```

### Hypertable Pattern
```sql
-- Create standard table first
CREATE TABLE crypto_scout.bybit_spot_kline_1m (
    time TIMESTAMPTZ NOT NULL,
    symbol TEXT NOT NULL,
    open DOUBLE PRECISION NOT NULL,
    high DOUBLE PRECISION NOT NULL,
    low DOUBLE PRECISION NOT NULL,
    close DOUBLE PRECISION NOT NULL,
    volume DOUBLE PRECISION NOT NULL,
    turnover DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (time, symbol)
);

-- Convert to hypertable
SELECT create_hypertable('crypto_scout.bybit_spot_kline_1m', 'time');
```

## Table Definitions

### Kline Tables (Candlestick Data)
```sql
CREATE TABLE crypto_scout.bybit_spot_kline_1m (
    time TIMESTAMPTZ NOT NULL,
    symbol TEXT NOT NULL,
    open DOUBLE PRECISION NOT NULL,
    high DOUBLE PRECISION NOT NULL,
    low DOUBLE PRECISION NOT NULL,
    close DOUBLE PRECISION NOT NULL,
    volume DOUBLE PRECISION NOT NULL,
    turnover DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (time, symbol)
);

-- Indexes for common queries
CREATE INDEX idx_bybit_spot_kline_1m_symbol_time
ON crypto_scout.bybit_spot_kline_1m (symbol, time DESC);
```

### Ticker Tables
```sql
CREATE TABLE crypto_scout.bybit_spot_tickers (
    time TIMESTAMPTZ NOT NULL,
    symbol TEXT NOT NULL,
    last_price DOUBLE PRECISION NOT NULL,
    high_price_24h DOUBLE PRECISION NOT NULL,
    low_price_24h DOUBLE PRECISION NOT NULL,
    volume_24h DOUBLE PRECISION NOT NULL,
    turnover_24h DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (time, symbol)
);

SELECT create_hypertable('crypto_scout.bybit_spot_tickers', 'time');
```

### Trade Tables
```sql
CREATE TABLE crypto_scout.bybit_spot_public_trade (
    time TIMESTAMPTZ NOT NULL,
    symbol TEXT NOT NULL,
    trade_id TEXT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    qty DOUBLE PRECISION NOT NULL,
    side TEXT NOT NULL,
    PRIMARY KEY (time, trade_id)
);

SELECT create_hypertable('crypto_scout.bybit_spot_public_trade', 'time');
```

### Order Book Tables
```sql
CREATE TABLE crypto_scout.bybit_spot_order_book_200 (
    time TIMESTAMPTZ NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,  -- 'bid' or 'ask'
    price DOUBLE PRECISION NOT NULL,
    qty DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (time, symbol, side, price)
);

SELECT create_hypertable('crypto_scout.bybit_spot_order_book_200', 'time');
```

### Liquidation Table (Linear only)
```sql
CREATE TABLE crypto_scout.bybit_linear_all_liquidation (
    time TIMESTAMPTZ NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    qty DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (time, symbol, side, price)
);

SELECT create_hypertable('crypto_scout.bybit_linear_all_liquidation', 'time');
```

### Stream Offsets (exactly-once processing)
```sql
CREATE TABLE crypto_scout.stream_offsets (
    stream_name TEXT PRIMARY KEY,
    offset_value BIGINT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Fear & Greed Index
```sql
CREATE TABLE crypto_scout.cmc_fgi (
    time TIMESTAMPTZ NOT NULL,
    value INTEGER NOT NULL,
    value_classification TEXT NOT NULL,
    PRIMARY KEY (time)
);

SELECT create_hypertable('crypto_scout.cmc_fgi', 'time');
```

## Performance Optimization

### Compression Policy
```sql
-- Enable compression after data ages
ALTER TABLE crypto_scout.bybit_spot_kline_1m
SET (timescaledb.compress, timescaledb.compress_segmentby = 'symbol');

-- Auto-compress after 7 days
SELECT add_compression_policy(
    'crypto_scout.bybit_spot_kline_1m',
    INTERVAL '7 days'
);
```

### Retention Policy
```sql
-- Remove old data after 90 days
SELECT add_retention_policy(
    'crypto_scout.bybit_spot_kline_1m',
    INTERVAL '90 days'
);
```

### Reorder Policy
```sql
-- Optimize query performance for recent data
SELECT add_reorder_policy(
    'crypto_scout.bybit_spot_kline_1m',
    'idx_bybit_spot_kline_1m_symbol_time'
);
```

## JDBC Operations

### Repository Pattern (crypto-scout-collector)
```java
public final class BybitSpotRepository {
    private final DataSource dataSource;

    public void saveTickers(final List<Ticker> tickers) throws SQLException {
        final var sql = "INSERT INTO crypto_scout.bybit_spot_tickers " +
            "(time, symbol, last_price, high_price_24h, low_price_24h, volume_24h, turnover_24h) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (time, symbol) DO NOTHING";

        try (final var conn = dataSource.getConnection();
             final var stmt = conn.prepareStatement(sql)) {

            for (final var ticker : tickers) {
                stmt.setTimestamp(1, Timestamp.from(ticker.time()));
                stmt.setString(2, ticker.symbol());
                stmt.setDouble(3, ticker.lastPrice());
                stmt.setDouble(4, ticker.highPrice24h());
                stmt.setDouble(5, ticker.lowPrice24h());
                stmt.setDouble(6, ticker.volume24h());
                stmt.setDouble(7, ticker.turnover24h());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }
}
```

### DataSource Configuration (HikariCP)
```java
// CollectorDataSource.java or AnalystDataSource.java
public final class CollectorDataSource {
    private static final HikariConfig CONFIG = new HikariConfig();
    private static final HikariDataSource DATA_SOURCE;

    static {
        CONFIG.setJdbcUrl(JdbcConfig.JDBC_DATASOURCE_URL);
        CONFIG.setUsername(JdbcConfig.JDBC_DATASOURCE_USER);
        CONFIG.setPassword(JdbcConfig.JDBC_DATASOURCE_PASSWORD);
        CONFIG.setMaximumPoolSize(10);
        CONFIG.addDataSourceProperty("reWriteBatchedInserts", "true");
        CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        DATA_SOURCE = new HikariDataSource(CONFIG);
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }
}
```

### Offset Management (exactly-once)
```java
public void saveWithOffset(
    final List<Data> data,
    final String streamName,
    final long offset
) throws SQLException {
    final var insertSql = "INSERT INTO ...";
    final var offsetSql =
        "INSERT INTO crypto_scout.stream_offsets (stream_name, offset_value) " +
        "VALUES (?, ?) " +
        "ON CONFLICT (stream_name) DO UPDATE SET offset_value = EXCLUDED.offset_value";

    try (final var conn = dataSource.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // Insert data
            try (final var stmt = conn.prepareStatement(insertSql)) {
                for (final var d : data) {
                    // set parameters
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // Update offset
            try (final var stmt = conn.prepareStatement(offsetSql)) {
                stmt.setString(1, streamName);
                stmt.setLong(2, offset);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }
}
```

## Query Patterns

### Time-Range Queries
```sql
-- Get klines for last 24 hours
SELECT * FROM crypto_scout.bybit_spot_kline_1m
WHERE symbol = 'BTCUSDT'
  AND time >= NOW() - INTERVAL '24 hours'
ORDER BY time DESC;

-- Get aggregated daily data
SELECT
    time_bucket('1 day', time) AS day,
    symbol,
    first(open, time) AS open,
    max(high) AS high,
    min(low) AS low,
    last(close, time) AS close,
    sum(volume) AS volume
FROM crypto_scout.bybit_spot_kline_1m
WHERE symbol = 'BTCUSDT'
GROUP BY day, symbol
ORDER BY day DESC;
```

### Continuous Aggregates
```sql
-- Create 1-hour continuous aggregate
CREATE MATERIALIZED VIEW crypto_scout.bybit_spot_kline_1h
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', time) AS bucket,
    symbol,
    first(open, time) AS open,
    max(high) AS high,
    min(low) AS low,
    last(close, time) AS close,
    sum(volume) AS volume,
    sum(turnover) AS turnover
FROM crypto_scout.bybit_spot_kline_1m
GROUP BY bucket, symbol;

-- Refresh policy
SELECT add_continuous_aggregate_policy(
    'crypto_scout.bybit_spot_kline_1h',
    start_offset => INTERVAL '1 month',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour'
);
```

### Latest Data Queries
```sql
-- Get latest ticker for each symbol
SELECT DISTINCT ON (symbol)
    symbol,
    last_price,
    time
FROM crypto_scout.bybit_spot_tickers
ORDER BY symbol, time DESC;

-- Get data for specific time range
SELECT * FROM crypto_scout.cmc_fgi
WHERE time >= '2024-01-01'
  AND time < '2024-02-01'
ORDER BY time;
```

## Configuration

### System Properties
```java
final static class JdbcConfig {
    static final String JDBC_DATASOURCE_URL = System.getProperty(
        "jdbc.datasource.url",
        "jdbc:postgresql://localhost:5432/crypto_scout"
    );
    static final String JDBC_DATASOURCE_USER = System.getProperty(
        "jdbc.datasource.user",
        "crypto_scout_db"
    );
    static final String JDBC_DATASOURCE_PASSWORD = System.getProperty(
        "jdbc.datasource.password",
        ""
    );
}
```

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `JDBC_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/crypto_scout` | Database URL |
| `JDBC_DATASOURCE_USER` | `crypto_scout_db` | Database user |
| `JDBC_DATASOURCE_PASSWORD` | - | Database password |

## Backup and Restore

### Backup
```bash
# Using pg_dump
pg_dump -h localhost -p 5432 -U crypto_scout_db -d crypto_scout > backup.sql

# Using backup sidecar (configured in compose)
# Backups are written to ./backups automatically
```

### Restore
```bash
# From SQL file
psql -h localhost -p 5432 -U crypto_scout_db -d crypto_scout < backup.sql

# From custom format
pg_restore -h localhost -p 5432 -U crypto_scout_db -d crypto_scout backup.dump
```

## Docker/Podman Compose Configuration

```yaml
services:
  crypto-scout-collector-db:
    image: timescale/timescaledb:latest-pg17
    container_name: crypto-scout-collector-db
    ports:
      - "127.0.0.1:5432:5432"
    volumes:
      - "./data/postgresql:/var/lib/postgresql/data"
      - "./script/init.sql:/docker-entrypoint-initdb.d/00-init.sql:ro"
    env_file:
      - ./secret/timescaledb.env
    networks:
      - crypto-scout-bridge
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U crypto_scout_db -d crypto_scout"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
```

## Troubleshooting

### Connection Issues
```bash
# Check if database is ready
podman exec crypto-scout-collector-db pg_isready -U crypto_scout_db

# Connect to database
podman exec -it crypto-scout-collector-db psql -U crypto_scout_db -d crypto_scout

# List tables
\dt crypto_scout.*

# Check table sizes
SELECT pg_size_pretty(pg_total_relation_size('crypto_scout.bybit_spot_kline_1m'));
```

### Performance Issues
```sql
-- Check chunk sizes
SELECT chunk_name, pg_size_pretty(total_bytes)
FROM chunks_detailed_size('crypto_scout.bybit_spot_kline_1m');

-- Check compression status
SELECT hypertable_name, compression_enabled
FROM timescaledb_information.hypertables;
```

## When to Use Me

Use this skill when:
- Designing new database tables for time-series data
- Implementing repository classes with JDBC
- Configuring hypertables and compression
- Setting up retention policies
- Optimizing query performance
- Managing stream offsets for exactly-once processing
- Planning backup and recovery strategies
- Working with HikariCP connection pooling
- Writing time-range queries
- Setting up continuous aggregates
