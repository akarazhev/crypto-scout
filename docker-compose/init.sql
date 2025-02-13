-- Create a schema
CREATE SCHEMA IF NOT EXISTS cryptoscout;

-- Set the search path
SET search_path TO cryptoscout;

-- Create tables
CREATE TABLE bybit_events (
    id SERIAL PRIMARY KEY,
    platform VARCHAR(50) NOT NULL,
    event_time BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    tags TEXT[],
    url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for frequently queried columns
CREATE INDEX idx_events_platform ON events(platform);
CREATE INDEX idx_events_type ON events(event_type);
CREATE INDEX idx_events_time ON events(event_time);

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA cryptoscout TO sa;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA cryptoscout TO sa;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA cryptoscout TO sa;