-- Create a schema
CREATE SCHEMA IF NOT EXISTS cryptoscout;

-- Set the search path
SET search_path TO cryptoscout;

-- Create tables
CREATE TABLE IF NOT EXISTS bybit_events (
    id SERIAL PRIMARY KEY,
    event_time BIGINT NOT NULL,
    publish_time BIGINT NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    tags TEXT[],
    url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for frequently queried columns
CREATE INDEX IF NOT EXISTS idx_bybit_events_time ON bybit_events(event_time, publish_time, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_bybit_events_type ON bybit_events(event_type);

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA cryptoscout TO sa;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA cryptoscout TO sa;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA cryptoscout TO sa;