---
name: project-architecture
description: High-level architecture and design patterns for the crypto-scout ecosystem
license: MIT
compatibility: opencode
metadata:
  domain: architecture
  pattern: microservices
  style: event-driven
---

## What I Do

Provide comprehensive guidance on the architecture, design patterns, and system interactions within the crypto-scout ecosystem.

## System Overview

```mermaid
flowchart TB
    subgraph External["External APIs"]
        Bybit["Bybit API<br/>WebSocket + REST"]
        CMC["CoinMarketCap API<br/>REST"]
    end

    subgraph Library["Core Library"]
        JCL["jcryptolib v0.0.4<br/>(Bybit Stream, CMC Parser,<br/>Analysis Engine)"]
    end

    subgraph Messaging["RabbitMQ Messaging"]
        BS["bybit-stream"]
        CS["crypto-scout-stream"]
        CQ["collector-queue"]
    end

    subgraph Services["Microservices"]
        Client["crypto-scout-client v0.0.1<br/>(Data Collection)"]
        Collector["crypto-scout-collector v0.0.1<br/>(Data Persistence)"]
        Analyst["crypto-scout-analyst v0.0.1<br/>(Analysis)"]
    end

    subgraph Storage["Data Storage"]
        DB[("TimescaleDB<br/>Time-series data")]
    end

    Bybit -->|WebSocket| JCL
    CMC -->|REST| JCL
    JCL -->|Uses| Client
    Client -->|Publish| BS
    Client -->|Publish| CS
    BS -->|Consume| Collector
    CS -->|Consume| Collector
    Collector -->|JDBC| DB
    BS -->|Consume| Analyst
    CS -->|Consume| Analyst
    CQ -->|AMQP| Collector
```

## Module Responsibilities

### jcryptolib (v0.0.4)
**Purpose**: Core cryptocurrency library shared across all services

**Components**:
- **Bybit Streaming** (`bybit/stream/`): WebSocket client with resilience patterns
  - `BybitStream`: Main streaming class with auto-reconnect, ping/pong
  - `BybitParser`: REST API data fetching
  - `PingPongHandler`: WebSocket heartbeat management
  - `Requests`/`Responses`: Message builders/parsers
- **CMC Parser** (`cmc/parser/`): REST API client with scheduling
  - `CmcParser`: Main parser with rate limiting
  - `CmcConfig`: Configuration management
- **Analysis Engine** (`analysis/engine/`): Technical indicators
  - `AnalystEngine`: Main analysis orchestrator
  - `SmaIndicator`: Simple Moving Average
  - `EmaIndicator`: Exponential Moving Average
  - `BitcoinRiskIndicator`: BTC risk assessment
- **Resilience** (`resilience/`): Circuit breaker, rate limiter, health checks
- **Stream Abstractions** (`stream/`): Payload, Message, Provider, Source, Statistic
- **Utils** (`util/`): JsonUtils, ParserUtils, TimeUtils, ValueUtils, SecUtils
- **Exceptions** (`exception/`): 10 exception types with hierarchy

**Usage**: Dependency for all other Java modules

### crypto-scout-test (v0.0.1)
**Purpose**: Shared test utilities library

**Components**:
- `MockData`: Typed access to JSON test fixtures (bybit-spot, bybit-linear, crypto-scout)
- `PodmanCompose`: Container lifecycle management
- `StreamTestPublisher`/`StreamTestConsumer`: RabbitMQ Streams test utilities
- `AmqpTestPublisher`/`AmqpTestConsumer`: AMQP test utilities
- `DBUtils`: Database operations for tests
- `Assertions`: Custom test assertions

**Usage**: Test-scoped dependency in collector and analyst

### crypto-scout-client (v0.0.1)
**Purpose**: Real-time market data collection

**Data Sources**:
- **Bybit Spot**: BTCUSDT, ETHUSDT (tickers, trades, order books, klines)
- **Bybit Linear**: BTCUSDT, ETHUSDT (tickers, trades, liquidations, klines)
- **CoinMarketCap**: Fear & Greed Index, BTC/USD quotes

**Architecture**:
```
Client (Launcher)
├── CoreModule (Reactor, Executor)
├── WebModule (HTTP server, Health)
├── ClientModule (AmqpPublisher)
├── BybitSpotModule (WebSocket consumers)
│   ├── BybitSpotBtcUsdtConsumer
│   └── BybitSpotEthUsdtConsumer
├── BybitLinearModule (WebSocket consumers)
│   ├── BybitLinearBtcUsdtConsumer
│   └── BybitLinearEthUsdtConsumer
├── CmcParserModule (HTTP parser)
│   └── CmcParserConsumer
└── JmxModule (Monitoring)
```

**Publishing Strategy**:
- Bybit data → `bybit-stream`
- CMC data → `crypto-scout-stream`

### crypto-scout-collector (v0.0.1)
**Purpose**: Data persistence and storage

**Stream Consumers**:
- `bybit-stream` → `BybitStreamService`
- `crypto-scout-stream` → `CryptoScoutService`
- `collector-queue` (AMQP) → `AmqpConsumer`

**Data Flow**:
```
StreamService
├── BybitStreamService
│   ├── BybitSpotRepository (spot tables)
│   └── BybitLinearRepository (linear tables)
└── CryptoScoutService
    └── CryptoScoutRepository (fgi, klines, risk)

AmqpConsumer
└── Command/Control messages
```

**Repositories**:
- `BybitSpotRepository`: Spot market data
- `BybitLinearRepository`: Linear/perp market data
- `CryptoScoutRepository`: CMC/analysis data
- `AnalystRepository`: Analyst-specific tables
- `StreamOffsetsRepository`: Offset tracking

**Offset Management**: DB-backed offsets for exactly-once processing

### crypto-scout-analyst (v0.0.1)
**Purpose**: Market analysis and alerting

**Architecture**:
```
Analyst (Launcher)
├── CoreModule (Reactor, Executor)
├── WebModule (HTTP server, Health)
├── AnalystModule (Analysis services)
│   ├── StreamService
│   │   ├── BybitStreamService
│   │   └── CryptoScoutService
│   ├── Stream transformers
│   │   ├── BytesToPayloadTransformer
│   │   └── AnalystTransformer
│   ├── DataService (async processing)
│   └── StreamPublisher (output)
└── JmxModule (Monitoring)
```

**Stream Processing Pipeline**:
```
RabbitMQ Stream → Consumer → BytesToPayloadTransformer → AnalystTransformer → DataService → Output
```

**Components**:
- `StreamService`: Orchestrates stream consumption
- `CryptoScoutService`: Consumes from crypto-scout-stream with transformers
- `BybitStreamService`: Consumes from bybit-stream
- `DataService`: Processes payloads asynchronously
- `AnalystTransformer`: Stream transformer for preprocessing
- `StreamPublisher`: Output publisher

### crypto-scout-mq
**Purpose**: Messaging infrastructure (not a Java module)

**Components**:
- RabbitMQ 4.1.4 with Streams and AMQP plugins
- Pre-configured exchanges, queues, and streams
- Stream retention policies (1 day, 2GB max)
- Dead-letter exchange for failed messages

**Streams**:
| Stream | Purpose | Retention |
|--------|---------|-----------|
| `bybit-stream` | Bybit market data | 1 day, 2GB max |
| `crypto-scout-stream` | CMC/parser data | 1 day, 2GB max |

**Queues**:
| Queue | Purpose | Arguments |
|-------|---------|-----------|
| `collector-queue` | Command/control messages | lazy mode, TTL 6h, max 2500 |
| `chatbot-queue` | Chatbot notifications | lazy mode, TTL 6h, max 2500 |
| `dlx-queue` | Dead letter handling | lazy mode, TTL 7d |

**Deployment**: Podman Compose with persistent volumes

## Data Flow Patterns

### 1. Market Data Ingestion
```
Bybit WebSocket → crypto-scout-client → bybit-stream → crypto-scout-collector → TimescaleDB
```

### 2. Metrics Collection
```
CMC REST API → crypto-scout-client → crypto-scout-stream → crypto-scout-collector → TimescaleDB
```

### 3. Analysis Pipeline
```
Streams → crypto-scout-analyst → [Transformers] → DataService → Output/Alerts
```

### 4. Command/Control
```
External → collector-queue (AMQP) → crypto-scout-collector → Action
```

## Design Patterns

### 1. Launcher Pattern (ActiveJ)
```java
public final class Service extends Launcher {
    @Override
    protected Module getModule() {
        return combine(
            CoreModule.create(),      // Reactor + Executor
            ServiceModule.create(),   // Service-specific
            WebModule.create()        // HTTP + Health
        );
    }

    @Override
    protected void run() throws Exception {
        awaitShutdown();  // Block until SIGTERM
    }
}
```

### 2. Reactive Service Pattern
```java
public final class MyService extends AbstractReactive implements ReactiveService {
    @Override
    public Promise<Void> start() {
        return Promise.ofBlocking(executor, () -> {
            // Initialize resources
        });
    }

    @Override
    public Promise<Void> stop() {
        return Promise.ofBlocking(executor, () -> {
            // Cleanup resources
        });
    }
}
```

### 3. Repository Pattern
```java
public final class DataRepository {
    private final DataSource dataSource;

    public void saveBatch(final List<Data> data) throws SQLException {
        try (final var conn = dataSource.getConnection();
             final var stmt = conn.prepareStatement(SQL)) {
            for (final var d : data) {
                // set parameters
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
```

### 4. Stream Transformer Pattern
```java
public final class AnalystTransformer extends AbstractStreamTransformer<StreamPayload, StreamPayload> {
    @Override
    protected StreamDataAcceptor<StreamPayload> onResumed(final StreamDataAcceptor<StreamPayload> output) {
        return in -> {
            final var result = process(in);
            output.accept(result);
        };
    }
}
```

### 5. Factory Pattern
```java
public final class Service {
    public static Service create(final NioReactor reactor,
                                  final Executor executor) {
        return new Service(reactor, executor);
    }

    private Service(final NioReactor reactor, final Executor executor) {
        // Private constructor
    }
}
```

## Configuration Management

### Hierarchy
1. **Bundled defaults**: `src/main/resources/application.properties`
2. **Environment variables**: Override via env vars
3. **System properties**: Override via `-D` flags

### Property Naming
```
server.port → SERVER_PORT
amqp.rabbitmq.host → AMQP_RABBITMQ_HOST
jdbc.datasource.url → JDBC_DATASOURCE_URL
```

### Pattern
```java
static final String VALUE = System.getProperty("key", "default");
```

## Error Handling Strategy

### Service Level
- Use `IllegalStateException` for invalid states
- Chain exceptions with causes
- Log errors with context

### Connection Level
- Retry with exponential backoff
- Circuit breaker pattern (jcryptolib)
- Graceful degradation

### Data Level
- Batch inserts with rollback
- Offset tracking for recovery
- Dead-letter queue for failures

## Scalability Considerations

### Horizontal Scaling
- **Client**: Multiple instances (stateless)
- **Collector**: Single instance per stream (offset management)
- **Analyst**: Multiple instances (consumer groups possible)

### Vertical Scaling
- **CPU**: Virtual threads for I/O bound work
- **Memory**: Bounded queues and streams
- **Storage**: Compression and retention policies

### Bottlenecks
- Database write capacity
- Network bandwidth
- Message broker throughput

## Security Architecture

### Network
- Internal network: `crypto-scout-bridge`
- No external exposure for services
- RabbitMQ management on localhost only

### Credentials
- Environment variables for secrets
- No hardcoded credentials
- File permissions on secret files (600)

### Container Security
- Non-root user (UID 10001)
- Read-only root filesystem
- Dropped capabilities
- No new privileges

## Monitoring and Observability

### Health Checks
```
GET /health → "ok" (200) or "not-ready" (503)
```

### Metrics
- JMX via ActiveJ
- Message throughput
- Database connection pool
- Error rates

### Logging
- SLF4J with Logback
- Structured logging
- Correlation IDs

## Deployment Strategy

### Development
```bash
# Local with Podman Compose
podman-compose up -d  # All services
```

### Production
```bash
# Individual service deployment
podman-compose up -d crypto-scout-client
podman-compose up -d crypto-scout-collector
podman-compose up -d crypto-scout-analyst
```

### Upgrade Process
1. Build new image
2. Health check on staging
3. Rolling update (stop old, start new)
4. Verify health endpoints

## When to Use Me

Use this skill when:
- Understanding the overall system architecture
- Designing new features across modules
- Planning service interactions
- Considering scalability and deployment
- Reviewing security implications
- Troubleshooting cross-module issues
- Documenting system behavior
- Implementing stream processing pipelines
