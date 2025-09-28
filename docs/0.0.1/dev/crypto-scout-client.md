# Crypto Scout Client Documentation

## Overview

The Crypto Scout Client is a Java-based service that collects cryptocurrency data from various sources and publishes it
to RabbitMQ queues for further processing. It's built using Spring Boot and follows a reactive programming model with
RxJava.

## Architecture

The service follows a modular architecture with the following key components:

### Core Components

1. **Data Collection**
    - Uses Java HTTP Client to fetch data from cryptocurrency APIs
    - Supports multiple data providers (CoinMarketCap, Bybit)

2. **Stream Processing**
    - Uses RxJava for reactive stream processing
    - Implements backpressure handling and error recovery

3. **Message Publishing**
    - Publishes processed data to RabbitMQ queues
    - Supports different routing based on data source and type

## Component Diagram

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│  Data Sources   │─────▶│  Data Stream    │─────▶│  RabbitMQ       │
│  (APIs)         │      │  Processing     │      │  Queues         │
│                 │      │                 │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

## Key Classes

- **CryptoScoutClient**: Main application class
- **DataBridge**: Connects data streams to publishers
- **DataStreamService**: Manages data stream processing
- **DataSupplier**: Provides data from different sources
- **DataPublisher**: Publishes data to RabbitMQ
- **ClientSubscriber**: Subscribes to control commands

## Configuration

### Application Properties

The service is configured via `application.properties` with the following key settings:

- **HTTP Client Configuration**
    - `client.connect.timeout.ms`: Connection timeout in milliseconds

- **CoinMarketCap Settings**
    - `cmc.connect.timeout.ms`: API connection timeout
    - `cmc.fetch.interval.ms`: Data fetch interval
    - `cmc.circuit.breaker.*`: Circuit breaker settings
    - `cmc.retry.*`: Retry settings
    - `cmc.api.key`: API key for authentication

- **RabbitMQ Settings**
    - `amqp.exchange.*`: Exchange configuration
    - `amqp.queue.*`: Queue configuration
    - `amqp.rabbitmq.*`: Connection settings

## Deployment

### Docker

The service can be deployed as a Docker container using the provided Dockerfile:

```dockerfile
FROM eclipse-temurin:21.0.8_9-jre-ubi9-minimal
WORKDIR /app
COPY target/crypto-scout-client-0.0.1.jar crypto-scout-client.jar
ENTRYPOINT ["java", "-jar", "crypto-scout-client.jar"]
```

### Environment Variables

The following environment variables can be used to configure the service:

- `AMQP_RABBITMQ_HOST`: RabbitMQ host (default: localhost)
- `AMQP_RABBITMQ_PORT`: RabbitMQ port (default: 5672)

## Development Guide

### Prerequisites

- Java 21
- Maven
- RabbitMQ

### Building the Service

```bash
mvn clean package
```

### Running the Service

```bash
java -jar target/crypto-scout-client-0.0.1.jar
```

### Docker Build and Run

```bash
docker build -t crypto-scout-client .
docker run -e AMQP_RABBITMQ_HOST=rabbitmq -e AMQP_RABBITMQ_PORT=5672 crypto-scout-client
```

## Best Practices for Production Deployment

1. **Security**
    - Store sensitive configuration in secure vaults
    - Use TLS for RabbitMQ connections
    - Implement proper authentication

2. **Monitoring**
    - Add health checks
    - Implement metrics collection
    - Set up alerting

3. **Scaling**
    - Deploy multiple instances behind a load balancer
    - Implement proper resource limits

4. **Resilience**
    - Implement circuit breakers
    - Add retry mechanisms
    - Use dead letter queues

## Roadmap for Improvements

1. Add comprehensive test coverage
2. Implement health checks and monitoring
3. Enhance error handling and resilience
4. Add support for additional data sources
5. Implement metrics collection and dashboards
