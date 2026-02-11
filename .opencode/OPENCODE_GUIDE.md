# OpenCode Configuration Guide

Production-ready OpenCode configuration for the crypto-scout ecosystem.

## Quick Start

```bash
# Use the primary developer agent for most tasks
@developer

# Request code review
@reviewer

# Generate documentation
@writer
```

## Agents

### @developer (Primary)
Senior Java developer for the entire crypto-scout ecosystem.

**Use for:**
- Writing new features
- Refactoring code
- Implementing services
- Fixing bugs
- Adding tests

**Capabilities:**
- Full tool access (write, edit, bash, glob, grep, read, fetch, skill)
- Java 25 expertise
- ActiveJ framework knowledge
- Multi-module Maven projects

### @reviewer (Subagent)
Code reviewer specializing in Java microservices.

**Use for:**
- Code review requests
- Style compliance checking
- Security audits
- Best practice validation

**Limitations:**
- Read-only (no write/edit/bash)
- Provides feedback only

### @writer (Subagent)
Technical writer for documentation.

**Use for:**
- README creation
- API documentation
- Architecture diagrams
- Troubleshooting guides

**Limitations:**
- Documentation files only
- No source code changes

## Skills

### Core Skills

| Skill | Description | Use When |
|-------|-------------|----------|
| `java-ecosystem` | Java 25 + ActiveJ patterns | Writing Java code, understanding async I/O |
| `maven-build` | Multi-module Maven builds | Building, testing, packaging |
| `project-architecture` | System design patterns | Architecture decisions, module interactions |

### Infrastructure Skills

| Skill | Description | Use When |
|-------|-------------|----------|
| `podman-orchestration` | Container deployment | Setting up services, troubleshooting containers |
| `timescaledb-data` | Time-series database | Database design, queries, optimization |
| `rabbitmq-messaging` | Streams and AMQP | Messaging patterns, publishers, consumers |

## Common Workflows

### 1. Implementing a New Feature

```
@developer
I need to add a new stream consumer for Binance data in the collector module.
```

The developer agent will:
1. Analyze existing patterns (BybitStreamService)
2. Create new service class following conventions
3. Add repository for data persistence
4. Wire into StreamService
5. Add tests
6. Update documentation

### 2. Code Review

```
@reviewer
Please review the changes in crypto-scout-collector/src/main/java/...
```

The reviewer agent will:
1. Check code style compliance
2. Verify error handling
3. Assess test coverage
4. Identify security issues
5. Provide structured feedback

### 3. Documentation Update

```
@writer
Update the README for crypto-scout-collector with the new database tables.
```

The writer agent will:
1. Follow existing README structure
2. Add accurate table documentation
3. Include code examples
4. Update configuration reference

### 4. Troubleshooting

```
@developer
The collector is failing to connect to RabbitMQ. Help me debug.
```

The developer agent will:
1. Check configuration patterns
2. Review connection code
3. Verify environment setup
4. Suggest fixes

## Project Structure Reference

```
crypto-scout/
├── pom.xml                      # Root aggregator POM (version 0.0.1)
│
├── jcryptolib/                  # Core cryptocurrency library (v0.0.4)
│   ├── src/main/java/.../jcryptolib/
│   │   ├── bybit/
│   │   │   ├── stream/          # BybitStream, BybitParser, PingPongHandler
│   │   │   └── config/          # Config, Type, StreamType, Topic
│   │   ├── cmc/
│   │   │   ├── parser/          # CmcParser, CmcConfig
│   │   │   └── config/          # Config, Type, Range
│   │   ├── analysis/
│   │   │   ├── engine/          # AnalystEngine, indicators (SMA, EMA, BitcoinRisk)
│   │   │   └── config/          # Type
│   │   ├── stream/              # Payload, Message, Provider, Source, Statistic
│   │   ├── resilience/          # CircuitBreaker, RateLimiter, HealthCheck
│   │   ├── util/                # JsonUtils, ParserUtils, TimeUtils, ValueUtils, SecUtils
│   │   ├── config/              # AppConfig
│   │   └── exception/           # 10 exception types
│   └── pom.xml
│
├── crypto-scout-test/           # Test library
│   ├── src/main/java/.../test/
│   │   ├── MockData.java
│   │   ├── PodmanCompose.java
│   │   ├── StreamTestPublisher.java
│   │   ├── StreamTestConsumer.java
│   │   ├── AmqpTestPublisher.java
│   │   ├── AmqpTestConsumer.java
│   │   ├── DBUtils.java
│   │   └── Assertions.java
│   └── src/main/resources/
│       ├── bybit-spot/          # Mock data (tickers, klines, orderbooks, trades)
│       ├── bybit-linear/        # Mock data (tickers, klines, orderbooks, liquidations)
│       ├── crypto-scout/        # Mock data (fgi, lpl, btcPriceRisk, btcRiskPrice)
│       └── podman/              # RabbitMQ and TimescaleDB configs for tests
│
├── crypto-scout-client/         # Data collection service
│   ├── src/main/java/.../
│   │   ├── Client.java          # Launcher
│   │   ├── client/
│   │   │   ├── AmqpPublisher.java
│   │   │   ├── AbstractBybitStreamConsumer.java
│   │   │   ├── BybitSpotBtcUsdtConsumer.java
│   │   │   ├── BybitSpotEthUsdtConsumer.java
│   │   │   ├── BybitLinearBtcUsdtConsumer.java
│   │   │   ├── BybitLinearEthUsdtConsumer.java
│   │   │   └── CmcParserConsumer.java
│   │   ├── config/              # AmqpConfig, WebConfig, CmcApiConfig
│   │   └── module/              # CoreModule, WebModule, ClientModule, BybitSpotModule, BybitLinearModule, CmcParserModule
│   ├── podman-compose.yml
│   └── Dockerfile
│
├── crypto-scout-collector/      # Data persistence service
│   ├── src/main/java/.../
│   │   ├── Collector.java       # Launcher
│   │   ├── collector/
│   │   │   ├── StreamService.java
│   │   │   ├── BybitStreamService.java
│   │   │   ├── CryptoScoutService.java
│   │   │   ├── DataService.java
│   │   │   ├── AnalystService.java
│   │   │   ├── AmqpPublisher.java
│   │   │   ├── AmqpConsumer.java
│   │   │   ├── HealthService.java
│   │   │   ├── MovingAverageCalculator.java
│   │   │   ├── PayloadParser.java
│   │   │   └── db/
│   │   │       ├── CollectorDataSource.java
│   │   │       ├── StreamOffsetsRepository.java
│   │   │       ├── BybitSpotRepository.java
│   │   │       ├── BybitLinearRepository.java
│   │   │       ├── CryptoScoutRepository.java
│   │   │       └── AnalystRepository.java
│   │   ├── config/              # AmqpConfig, JdbcConfig, ServerConfig
│   │   └── module/              # CoreModule, WebModule, CollectorModule
│   ├── script/                  # SQL scripts (init.sql, bybit_spot_tables.sql, etc.)
│   ├── podman-compose.yml
│   └── Dockerfile
│
├── crypto-scout-analyst/        # Analysis service
│   ├── src/main/java/.../
│   │   ├── Analyst.java         # Launcher
│   │   ├── stream/
│   │   │   ├── StreamService.java
│   │   │   ├── StreamPublisher.java
│   │   │   ├── StreamIn.java
│   │   │   ├── MessageSupplier.java
│   │   │   ├── StreamPayload.java
│   │   │   ├── BytesToPayloadTransformer.java
│   │   │   └── AnalystTransformer.java
│   │   ├── db/
│   │   │   ├── AnalystDataSource.java
│   │   │   └── StreamOffsetsRepository.java
│   │   ├── config/              # AmqpConfig, JdbcConfig, ServerConfig
│   │   └── module/              # CoreModule, WebModule, AnalystModule
│   ├── script/init.sql
│   ├── podman-compose.yml
│   └── Dockerfile
│
└── crypto-scout-mq/             # RabbitMQ infrastructure
    ├── rabbitmq/
    │   ├── definitions.json     # Exchange/queue/stream definitions
    │   ├── rabbitmq.conf        # RabbitMQ configuration
    │   └── enabled_plugins      # Stream plugin
    ├── script/
    ├── secret/
    └── podman-compose.yml
```

## Key Conventions

### Code Style
- MIT License header (23 lines)
- Package on line 25
- Imports: `java.*` → third-party → static
- `final var` for locals
- `IllegalStateException` for errors

### Testing
- JUnit 6 (Jupiter)
- `@BeforeAll` / `@AfterAll` lifecycle
- `should<Subject><Action>` test names
- `PodmanCompose.up()` / `down()` for integration tests

### Configuration
- System properties with defaults
- Env var override (UPPER_SNAKE_CASE)
- No hardcoded secrets

### Git
- One concern per commit
- Descriptive commit messages
- No secrets in repo

## Build Commands Reference

```bash
# Full build
mvn clean install

# Without tests
mvn -q -DskipTests install

# Single module
cd crypto-scout-client && mvn clean package

# Tests
mvn test
mvn test -Dtest=ClassName
mvn test -Dtest=ClassName#methodName

# With custom timeout
mvn -q -Dpodman.compose.up.timeout.min=5 test
```

## Service Ports Reference

| Service | Port | Protocol | Purpose |
|---------|------|----------|---------|
| RabbitMQ | 5672 | AMQP | Queue messaging |
| RabbitMQ | 5552 | Streams | Stream messaging |
| RabbitMQ | 15672 | HTTP | Management UI |
| TimescaleDB | 5432 | PostgreSQL | Database |
| Client | 8081 | HTTP | Health endpoint |
| Collector | 8081 | HTTP | Health endpoint |
| Analyst | 8081 | HTTP | Health endpoint |

## Troubleshooting

### Build Issues
```bash
# Clear local repo
rm -rf ~/.m2/repository/com/github/akarazhev
mvn clean install
```

### Container Issues
```bash
# Check logs
podman logs crypto-scout-mq

# Verify network
podman network inspect crypto-scout-bridge

# Restart services
podman-compose restart
```

### Database Issues
```bash
# Check connectivity
podman exec crypto-scout-collector-db pg_isready -U crypto_scout_db

# View tables
podman exec -it crypto-scout-collector-db psql -U crypto_scout_db -d crypto_scout -c "\dt crypto_scout.*"
```

## Security Checklist

- [ ] No hardcoded credentials
- [ ] Secret files have 600 permissions
- [ ] Environment variables for sensitive data
- [ ] Container runs as non-root
- [ ] Read-only root filesystem
- [ ] Dropped capabilities
- [ ] Network isolation

## Resources

- **Project Root**: `/Users/andrey.karazhev/Developer/startups/crypto-scout`
- **License**: MIT
- **Java Version**: 25
- **Maven**: 3.9+
- **Podman**: Latest stable

## Support

For issues or questions:
1. Check module-specific README.md
2. Review AGENTS.md in module directories
3. Consult skill documentation
4. Check test examples
