-- Create schema
CREATE SCHEMA IF NOT EXISTS crypto_scout;

-- Create extension for TimescaleDB (must be in public schema)
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Set the search path to include all necessary schemas
SET search_path TO public, crypto_scout;

-- Create Fear & Greed Index table in crypto_scout schema
CREATE TABLE IF NOT EXISTS crypto_scout.fgi (
    id BIGSERIAL,
    score INTEGER NOT NULL,
    name TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    btc_price NUMERIC(20, 2) NOT NULL,
    btc_volume NUMERIC(20, 2) NOT NULL,
    -- For hypertables, primary key must include the partitioning column (timestamp)
    CONSTRAINT fgi_pkey PRIMARY KEY (id, timestamp)
);

-- Create indexes for fgi table first (before hypertable conversion)
CREATE INDEX IF NOT EXISTS idx_fgi_timestamp ON crypto_scout.fgi(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_fgi_score ON crypto_scout.fgi(score);
CREATE INDEX IF NOT EXISTS idx_fgi_name ON crypto_scout.fgi(name);

-- Convert the fgi table to a hypertable partitioned by timestamp
-- Using 1-day chunks for optimal performance with daily data
SELECT public.create_hypertable('crypto_scout.fgi', 'timestamp', chunk_time_interval => INTERVAL '1 day');

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA crypto_scout TO sa;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA crypto_scout TO sa;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA crypto_scout TO sa;

-- Add compression after the table is created and permissions are granted
ALTER TABLE crypto_scout.fgi SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'name'
);

-- Compress chunks that are older than 7 days
SELECT add_compression_policy('crypto_scout.fgi', INTERVAL '7 days');

-- Optional: Add retention policy (uncomment if needed)
-- SELECT add_retention_policy('crypto_scout.fgi', INTERVAL '90 days');

-- Example insert statement for testing (uncomment to use)
-- INSERT INTO crypto_scout.fgi (score, name, timestamp, btc_price, btc_volume)
-- VALUES (
--     62,
--     'Greed',
--     to_timestamp(1753920000),
--     117833.63,
--     69370346017.86
-- );