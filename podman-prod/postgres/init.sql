-- Create a schema
CREATE SCHEMA IF NOT EXISTS cryptoscout;

-- Set the search path
SET search_path TO cryptoscout;

-- Create tables
CREATE TABLE IF NOT EXISTS bybit_event (
    id SERIAL PRIMARY KEY,
    event_time BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for frequently queried columns
CREATE INDEX IF NOT EXISTS idx_bybit_event_time ON bybit_event(event_time);
CREATE INDEX IF NOT EXISTS idx_bybit_title ON bybit_event(title);
CREATE INDEX IF NOT EXISTS idx_bybit_type ON bybit_event(type);

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA cryptoscout TO sa;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA cryptoscout TO sa;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA cryptoscout TO sa;