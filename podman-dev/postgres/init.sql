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