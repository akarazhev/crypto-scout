# Context: Rabbitmq Service

In this `crypto-scout` project we are going to use Rabbitmq for messaging between services and for crypto data
collection. So you will need to review and update the `podman-compose/podman-compose.yml` file for the Rabbitmq service.

## Roles

Take the following roles:

- Export dev-opts engineer.

## Conditions

- Use the best practices and design patterns.
- Do not hallucinate.
- Use the latest technology stack that you know.

## Tasks

- As the expert dev-opts engineer review the current Rabbitmq service implementation in
  `podman-compose/podman-compose.yml` and update it for Rabbitmq service if this is not production ready and contains
  missing parts.
- Use streams to process crypto data collection.
- Use common queue for messaging between services.
- Recheck your proposal and make sure that they are correct and haven't missed any important points.
- Write a report with your proposal and implementation into `doc/dev/rabbitmq-service-report.md`.

## Implementation Summary

The RabbitMQ service has been successfully implemented with the following key features:

### 1. Production-Ready Configuration

- **RabbitMQ 3.12** with management plugin
- Stream protocol support (port 5552)
- Resource limits and reservations for CPU and memory
- Health checks and proper restart policies
- File descriptor limits (ulimits)
- Memory high watermark (60%) and disk free limit (2GB)

### 2. Stream Processing for Crypto Data

- Enabled stream plugin
- Configured streams:
    - `crypto.price.stream`: For price updates
    - `crypto.market.stream`: For market data
- Stream settings:
    - 7-day data retention
    - 100MB segment size
    - 20GB total storage limit

### 3. Messaging Architecture

- **Exchanges**:
    - `metrics-exchange`: For metrics data
    - `crypto.events`: For crypto-related events
    - `service.events`: For service-to-service communication

- **Queues**:
    - `metrics-cmc-fear-greed-index-queue`: For CoinMarketCap fear and greed index metrics
    - `metrics-bybit-launch-pool-queue`: For Bybit launch pool metrics
    - `metrics-dead-letter-queue`: Dead letter queue for failed messages
    - `crypto-scout-client-queue`: Client-specific queue
    - `service.notifications`: For notifications (consumed by Telegram bot)
    - `service.commands`: For service commands

- **Bindings**:
    - Appropriate routing keys for all exchanges and queues
    - Dead letter exchange configuration

### 4. Client-Specific Configuration

- All queue and exchange names aligned with client code
- TTL and max length settings from application.properties
- Environment variables for all services

### 5. Security

- Credentials managed via Docker secrets
- Prepared TLS configuration (commented out)
- Proper user permissions

### 6. High Availability

- HA policies for queues and exchanges
- Automatic synchronization
- Clustering configuration

### 7. Configuration Files

- **enabled_plugins**: Management, prometheus, stream, consistent hash exchange, shovel plugins
- **rabbitmq.conf**: Memory/disk limits, stream settings, queue defaults, security
- **definitions.json**: Users, vhosts, permissions, exchanges, queues, bindings, streams

A detailed report is available in `docs/dev/rabbitmq-service-report.md`.