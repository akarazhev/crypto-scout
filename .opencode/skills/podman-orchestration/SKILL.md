---
name: podman-orchestration
description: Podman Compose orchestration for crypto-scout services including RabbitMQ, TimescaleDB, and microservices
license: MIT
compatibility: opencode
metadata:
  tool: podman
  orchestration: compose
  services: rabbitmq,timescaledb
---

## What I Do

Guide containerized deployment and orchestration of the crypto-scout ecosystem using Podman Compose.

## Service Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    crypto-scout-bridge network                   │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │ crypto-scout │  │   crypto-    │  │   crypto-    │           │
│  │     -mq      │  │ scout-client │  │scout-collector│           │
│  │  (RabbitMQ)  │  │ (Collection) │  │(Persistence) │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
│         │                                              │        │
│         │              ┌──────────────┐                │        │
│         │              │   crypto-    │                │        │
│         │              │scout-analyst │                │        │
│         │              │  (Analysis)  │                │        │
│         │              └──────────────┘                │        │
│         │                                              │        │
│  ┌──────┴────────┐                          ┌─────────┴───┐    │
│  │ crypto-scout  │                          │ crypto-scout│    │
│  │-collector-db  │                          │-collector   │    │
│  │ (TimescaleDB) │                          │  -backup    │    │
│  └───────────────┘                          │  (Backups)  │    │
│                                             └─────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## Prerequisites

```bash
# Install Podman
brew install podman  # macOS
sudo apt install podman  # Ubuntu

# Install podman-compose
pip3 install podman-compose
# or
brew install podman-compose

# Create network (once)
podman network create crypto-scout-bridge
```

## Service Definitions

### crypto-scout-mq (RabbitMQ)
```yaml
services:
  crypto-scout-mq:
    image: rabbitmq:4.1.4-management
    container_name: crypto-scout-mq
    hostname: crypto_scout_mq
    ports:
      - "127.0.0.1:15672:15672"  # Management UI (localhost only)
      - "5672:5672"              # AMQP
      - "5552:5552"              # Streams
    volumes:
      - "./data/rabbitmq:/var/lib/rabbitmq"
      - "./rabbitmq/enabled_plugins:/etc/rabbitmq/enabled_plugins:ro"
      - "./rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro"
      - "./rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro"
    env_file:
      - ./secret/rabbitmq.env
    networks:
      - crypto-scout-bridge
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
```

**Key Files:**
- `rabbitmq/definitions.json` - Exchanges, queues, streams configuration
- `rabbitmq/rabbitmq.conf` - RabbitMQ server configuration
- `rabbitmq/enabled_plugins` - Stream plugin enabled
- `secret/rabbitmq.env` - Erlang cookie

### crypto-scout-collector-db (TimescaleDB)
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

**SQL Scripts:**
- `script/init.sql` - Schema initialization
- `script/bybit_spot_tables.sql` - Spot market tables
- `script/bybit_linear_tables.sql` - Linear market tables
- `script/crypto_scout_tables.sql` - CMC/analysis tables
- `script/analyst_tables.sql` - Analysis tables

### crypto-scout-client (Data Collection Service)
```yaml
services:
  crypto-scout-client:
    image: localhost/crypto-scout-client:latest
    container_name: crypto-scout-client
    build:
      context: ../crypto-scout-client
      dockerfile: Dockerfile
    env_file:
      - ./secret/client.env
    networks:
      - crypto-scout-bridge
    depends_on:
      - crypto-scout-mq
    restart: unless-stopped
```

### crypto-scout-collector (Data Persistence Service)
```yaml
services:
  crypto-scout-collector:
    image: localhost/crypto-scout-collector:latest
    container_name: crypto-scout-collector
    build:
      context: ../crypto-scout-collector
      dockerfile: Dockerfile
    env_file:
      - ./secret/collector.env
    networks:
      - crypto-scout-bridge
    depends_on:
      - crypto-scout-mq
      - crypto-scout-collector-db
    restart: unless-stopped
```

### crypto-scout-analyst (Analysis Service)
```yaml
services:
  crypto-scout-analyst:
    image: localhost/crypto-scout-analyst:latest
    container_name: crypto-scout-analyst
    build:
      context: ../crypto-scout-analyst
      dockerfile: Dockerfile
    env_file:
      - ./secret/analyst.env
    networks:
      - crypto-scout-bridge
    depends_on:
      - crypto-scout-mq
      - crypto-scout-collector-db
    restart: unless-stopped
```

## Container Security Hardening

### Standard Security Options
```yaml
security_opt:
  - no-new-privileges=true
cap_drop:
  - ALL
read_only: true
tmpfs:
  - /tmp:rw,size=512m,mode=1777,nodev,nosuid
init: true
pids_limit: 256
ulimits:
  nofile:
    soft: 4096
    hard: 4096
stop_signal: SIGTERM
stop_grace_period: 30s
restart: unless-stopped
```

### Resource Limits
```yaml
cpus: "0.5"
mem_limit: "256m"
mem_reservation: "128m"
```

## Management Commands

### Start Services
```bash
# Create network first
podman network create crypto-scout-bridge

# Start infrastructure (RabbitMQ, TimescaleDB)
cd crypto-scout-mq && podman-compose up -d
cd crypto-scout-collector && podman-compose up -d crypto-scout-collector-db

# Start services
cd crypto-scout-client && podman-compose up -d
cd crypto-scout-collector && podman-compose up -d
cd crypto-scout-analyst && podman-compose up -d

# Start specific service
podman-compose up -d crypto-scout-mq

# Start with build
podman-compose up -d --build
```

### Monitor Services
```bash
# View running containers
podman ps

# Check logs
podman logs -f crypto-scout-mq
podman logs -f crypto-scout-client
podman logs -f crypto-scout-collector
podman logs -f crypto-scout-analyst

# Check health status
podman inspect --format='{{.State.Health.Status}}' crypto-scout-mq

# View compose status
podman-compose ps

# Resource usage
podman stats
```

### Stop Services
```bash
# Stop all services
podman-compose down

# Stop and remove volumes (destructive)
podman-compose down -v

# Stop specific service
podman-compose stop crypto-scout-client
```

## Secret Management

### Environment Files Structure
```bash
# crypto-scout-mq/secret/rabbitmq.env
RABBITMQ_ERLANG_COOKIE=strong_random_string

# crypto-scout-collector/secret/timescaledb.env
POSTGRES_DB=crypto_scout
POSTGRES_USER=crypto_scout_db
POSTGRES_PASSWORD=strong_password

# crypto-scout-client/secret/client.env
AMQP_RABBITMQ_PASSWORD=mq_password
BYBIT_API_KEY=your_api_key
BYBIT_API_SECRET=your_api_secret
CMC_API_KEY=your_cmc_api_key

# crypto-scout-collector/secret/collector.env
AMQP_RABBITMQ_PASSWORD=mq_password
JDBC_DATASOURCE_PASSWORD=db_password

# crypto-scout-analyst/secret/analyst.env
AMQP_RABBITMQ_PASSWORD=mq_password
JDBC_DATASOURCE_PASSWORD=db_password
```

### Secret Setup
```bash
# Copy example files
cp secret/rabbitmq.env.example secret/rabbitmq.env
cp secret/timescaledb.env.example secret/timescaledb.env
cp secret/client.env.example secret/client.env
cp secret/collector.env.example secret/collector.env
cp secret/analyst.env.example secret/analyst.env

# Set permissions
chmod 600 secret/*.env

# Edit with secure values
$EDITOR secret/rabbitmq.env
```

## Network Configuration

### External Network
```yaml
networks:
  crypto-scout-bridge:
    name: crypto-scout-bridge
    external: true
```

### Service Discovery
| Service | Hostname | Ports | Internal Access |
|---------|----------|-------|-----------------|
| RabbitMQ | crypto-scout-mq | 5672 (AMQP), 5552 (Streams), 15672 (Mgmt) | All services |
| TimescaleDB | crypto-scout-collector-db | 5432 (PostgreSQL) | collector, analyst |
| Client | crypto-scout-client | 8081 (HTTP internal) | Health checks |
| Collector | crypto-scout-collector | 8081 (HTTP internal) | Health checks |
| Analyst | crypto-scout-analyst | 8081 (HTTP internal) | Health checks |

## Troubleshooting

### Container Won't Start
```bash
# Check logs
podman logs crypto-scout-mq
podman logs crypto-scout-collector-db

# Check for port conflicts
lsof -i :5672
lsof -i :5432
lsof -i :5552
lsof -i :15672

# Verify network exists
podman network ls
podman network inspect crypto-scout-bridge
```

### Health Check Failing
```bash
# Manual health check
podman exec crypto-scout-mq rabbitmq-diagnostics -q ping
podman exec crypto-scout-collector-db pg_isready -U crypto_scout_db

# Check resource usage
podman stats
```

### Connectivity Issues
```bash
# Test network connectivity
podman exec crypto-scout-client ping crypto-scout-mq
podman exec crypto-scout-collector ping crypto-scout-collector-db

# Inspect container network
podman inspect crypto-scout-mq --format='{{.NetworkSettings.Networks}}'
```

### Database Issues
```bash
# Connect to database
podman exec -it crypto-scout-collector-db psql -U crypto_scout_db -d crypto_scout

# List tables
\dt crypto_scout.*

# Check table counts
SELECT COUNT(*) FROM crypto_scout.bybit_spot_tickers;
```

## When to Use Me

Use this skill when:
- Setting up the development environment
- Deploying services to production
- Configuring container security
- Managing secrets and environment variables
- Troubleshooting container issues
- Scaling or modifying service configurations
- Setting up CI/CD pipelines with Podman
- Managing service dependencies
