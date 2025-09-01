-- Create schema
CREATE SCHEMA IF NOT EXISTS crypto_scout;

-- Create extension for TimescaleDB (must be in public schema)
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Set the search path to include all necessary schemas
SET search_path TO public, crypto_scout;

-- Create Fear & Greed Index table in crypto_scout schema
CREATE TABLE IF NOT EXISTS crypto_scout.cmc_fgi (
    id BIGSERIAL,
    score INTEGER NOT NULL,
    name TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    btc_price NUMERIC(20, 2) NOT NULL,
    btc_volume NUMERIC(20, 2) NOT NULL,
    -- For hypertables, primary key must include the partitioning column (timestamp)
    CONSTRAINT fgi_pkey PRIMARY KEY (id, timestamp)
);

-- Create indexes for cmc_fgi table first (before hypertable conversion)
CREATE INDEX IF NOT EXISTS idx_cmc_fgi_timestamp ON crypto_scout.cmc_fgi(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_cmc_fgi_score ON crypto_scout.cmc_fgi(score);
CREATE INDEX IF NOT EXISTS idx_cmc_fgi_name ON crypto_scout.cmc_fgi(name);

-- Convert the cmc_fgi table to a hypertable partitioned by timestamp
-- Using 1-day chunks for optimal performance with daily data
SELECT public.create_hypertable('crypto_scout.cmc_fgi', 'timestamp', chunk_time_interval => INTERVAL '1 day');

-- Create Bybit Spot Tickers BTC/USDT table in crypto_scout schema
CREATE TABLE IF NOT EXISTS crypto_scout.bybit_spot_tickers_btc_usdt (
    id BIGSERIAL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    cross_sequence BIGINT NOT NULL,
    last_price NUMERIC(20, 2) NOT NULL,
    high_price_24h NUMERIC(20, 2) NOT NULL,
    low_price_24h NUMERIC(20, 2) NOT NULL,
    prev_price_24h NUMERIC(20, 2) NOT NULL,
    volume_24h NUMERIC(20, 8) NOT NULL,
    turnover_24h NUMERIC(20, 4) NOT NULL,
    price_24h_pcnt NUMERIC(10, 4) NOT NULL,
    usd_index_price NUMERIC(20, 6),
    -- For hypertables, primary key must include the partitioning column (timestamp)
    CONSTRAINT bybit_spot_tickers_btc_usdt_pkey PRIMARY KEY (id, timestamp)
);

-- Create indexes for bybit_spot_tickers_btc_usdt table first (before hypertable conversion)
CREATE INDEX IF NOT EXISTS idx_bybit_spot_tickers_btc_usdt_timestamp ON crypto_scout.bybit_spot_tickers_btc_usdt(timestamp DESC);

-- Create Bybit Spot Tickers ETH/USDT table in crypto_scout schema
CREATE TABLE IF NOT EXISTS crypto_scout.bybit_spot_tickers_eth_usdt (
    id BIGSERIAL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    cross_sequence BIGINT NOT NULL,
    last_price NUMERIC(20, 2) NOT NULL,
    high_price_24h NUMERIC(20, 2) NOT NULL,
    low_price_24h NUMERIC(20, 2) NOT NULL,
    prev_price_24h NUMERIC(20, 2) NOT NULL,
    volume_24h NUMERIC(20, 8) NOT NULL,
    turnover_24h NUMERIC(20, 4) NOT NULL,
    price_24h_pcnt NUMERIC(10, 4) NOT NULL,
    usd_index_price NUMERIC(20, 6),
    -- For hypertables, primary key must include the partitioning column (timestamp)
    CONSTRAINT bybit_spot_tickers_eth_usdt_pkey PRIMARY KEY (id, timestamp)
);

-- Create indexes for bybit_spot_tickers_eth_usdt table first (before hypertable conversion)
CREATE INDEX IF NOT EXISTS idx_bybit_spot_tickers_eth_usdt_timestamp ON crypto_scout.bybit_spot_tickers_eth_usdt(timestamp DESC);

-- Convert the bybit_spot_tickers_btc_usdt table to a hypertable partitioned by timestamp
-- Using 1-day chunks for optimal performance with time-series data
SELECT public.create_hypertable('crypto_scout.bybit_spot_tickers_btc_usdt', 'timestamp', chunk_time_interval => INTERVAL '1 day', if_not_exists => TRUE);

-- Convert the bybit_spot_tickers_eth_usdt table to a hypertable partitioned by timestamp
-- Using 1-day chunks for optimal performance with time-series data
SELECT public.create_hypertable('crypto_scout.bybit_spot_tickers_eth_usdt', 'timestamp', chunk_time_interval => INTERVAL '1 day', if_not_exists => TRUE);

-- Create Bybit Launch Pool table in crypto_scout schema
CREATE TABLE IF NOT EXISTS crypto_scout.bybit_lpl (
    id BIGSERIAL,
    return_coin VARCHAR(50) NOT NULL,
    return_coin_icon TEXT NOT NULL,
    description TEXT NOT NULL,
    website TEXT NOT NULL,
    whitepaper TEXT NOT NULL,
    rules TEXT NOT NULL,
    stake_begin_time TIMESTAMP WITH TIME ZONE NOT NULL,
    stake_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    trade_begin_time TIMESTAMP WITH TIME ZONE,
    -- For hypertables, primary key must include the partitioning column (stake_begin_time)
    CONSTRAINT bybit_lpl_pkey PRIMARY KEY (id, stake_begin_time)
);

-- Create indexes for bybit_lpl table first (before hypertable conversion)
CREATE INDEX IF NOT EXISTS idx_bybit_lpl_stake_begin_time ON crypto_scout.bybit_lpl(stake_begin_time DESC);
CREATE INDEX IF NOT EXISTS idx_bybit_lpl_return_coin ON crypto_scout.bybit_lpl(return_coin);

-- Convert the bybit_lpl table to a hypertable partitioned by stake_begin_time
-- Using 1-day chunks for optimal performance with time-series data
SELECT public.create_hypertable('crypto_scout.bybit_lpl', 'stake_begin_time', chunk_time_interval => INTERVAL '1 day');

-- Set up compression policy for bybit_spot_tickers_btc_usdt
ALTER TABLE crypto_scout.bybit_spot_tickers_btc_usdt SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'id'
);

-- Set up compression policy for bybit_spot_tickers_eth_usdt
ALTER TABLE crypto_scout.bybit_spot_tickers_eth_usdt SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'id'
);

-- Add compression for bybit_lpl table
ALTER TABLE crypto_scout.bybit_lpl SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'return_coin'
);

-- Compress chunks that are older than 7 days
SELECT add_compression_policy('crypto_scout.bybit_spot_tickers_btc_usdt', INTERVAL '7 days');
SELECT add_compression_policy('crypto_scout.bybit_lpl', INTERVAL '7 days');
SELECT add_compression_policy('crypto_scout.bybit_spot_tickers_eth_usdt', INTERVAL '7 days');

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA crypto_scout TO sa;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA crypto_scout TO sa;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA crypto_scout TO sa;

-- Add compression after the table is created and permissions are granted
ALTER TABLE crypto_scout.cmc_fgi SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'name'
);

-- Compress chunks that are older than 7 days
SELECT add_compression_policy('crypto_scout.cmc_fgi', INTERVAL '7 days');

-- Optional: Add retention policy (uncomment if needed)
-- SELECT add_retention_policy('crypto_scout.cmc_fgi', INTERVAL '90 days');
-- SELECT add_retention_policy('crypto_scout.bybit_spot_tickers_btc_usdt', INTERVAL '90 days');
-- SELECT add_retention_policy('crypto_scout.bybit_spot_tickers_eth_usdt', INTERVAL '90 days');
-- SELECT add_retention_policy('crypto_scout.bybit_lpl', INTERVAL '90 days');