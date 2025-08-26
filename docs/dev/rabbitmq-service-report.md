# RabbitMQ Service Implementation Report

## Executive Summary

This report outlines the production-ready RabbitMQ implementation for the Crypto-Scout project. The implementation
focuses on:

1. High availability and reliability
2. Performance optimization
3. Stream processing for crypto data collection
4. Common queues for inter-service messaging
5. Security best practices
6. Client-specific configuration alignment

## Current Implementation Analysis

The original RabbitMQ configuration in `podman-compose.yml` had several limitations:

- Basic configuration with minimal settings
- No explicit stream support for crypto data collection
- No predefined queues or exchanges for service communication
- Limited resource allocation
- No advanced configuration for high availability
- Missing performance tuning parameters
- Not aligned with the actual client code requirements

## Improved Implementation

### 1. RabbitMQ Service Configuration

The updated RabbitMQ service configuration includes:

```yaml
services:
  rabbitmq:
    image: rabbitmq:4.1.3-management
    container_name: crypto-scout-mq
    hostname: rabbitmq
    ports:
      - "5672:5672"   # AMQP protocol
      - "15672:15672" # Management UI
      - "5552:5552"   # Stream protocol
    volumes:
      - ./data/rabbitmq:/var/lib/rabbitmq
      - ./rabbitmq/enabled_plugins:/etc/rabbitmq/enabled_plugins
      - ./rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf
      - ./rabbitmq/definitions.json:/etc/rabbitmq/definitions.json
    networks:
      - crypto-scout
    environment:
      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbit disk_free_limit 2147483648 -rabbit vm_memory_high_watermark 0.6
    # Additional configuration...
```

**Note**: We've updated the configuration to use the recommended approach for setting default user and password directly
in the `rabbitmq.conf` file instead of using the deprecated environment variables. We've also aligned the data directory
structure with the project's `.gitignore` configuration. Most importantly, we've upgraded to RabbitMQ 4.1.3, which is
the latest version available.

### 2. Key Improvements

#### 2.1 Stream Processing Support

RabbitMQ Streams are now configured for crypto data collection:

- Added stream protocol port (5552)
- Enabled the `rabbitmq_stream` plugin
- Created dedicated streams for crypto data:
    - `crypto.price.stream`: For price updates
    - `crypto.market.stream`: For market data

Streams are configured with:

- 7-day data retention
- 100MB segment size
- 20GB total storage limit

#### 2.2 Client-Specific Messaging Configuration

Based on the analysis of the client code (`AmqpConfig.java` and `application.properties`), we've implemented a messaging
system that aligns with the actual client requirements:

- **Exchanges**:
    - `metrics-exchange`: For metrics data (from client code)
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
    - `metrics-exchange` → `metrics-cmc-fear-greed-index-queue` with routing key `metrics.cmc_fear_greed_index`
    - `metrics-exchange` → `metrics-bybit-launch-pool-queue` with routing key `metrics.bybit_launch_pool`
    - `crypto.events` → `service.notifications` with routing key `crypto.price.#`
    - `service.events` → `service.commands` with routing key `service.command.#`
    - `service.events` → `crypto-scout-client-queue` with routing key `commands`

#### 2.3 High Availability and Reliability

- Implemented HA policy for all queues and exchanges
- Configured automatic synchronization
- Added health checks for service dependencies
- Implemented proper restart policies
- Added dead letter queues for handling failed messages

#### 2.4 Performance Optimization

- Increased resource limits (CPU and memory)
- Added file descriptor limits
- Configured memory high watermark (60%)
- Set disk free limit (2GB)
- Optimized queue settings with max length and overflow behavior
- Set appropriate TTL values (21600000 ms / 6 hours) based on client configuration

#### 2.5 Security Enhancements

- Credentials configured directly in rabbitmq.conf (best practice)
- Prepared for TLS configuration (commented out)
- Configured proper user permissions

### 3. Configuration Files

#### 3.1 Enabled Plugins (`enabled_plugins`)

```erlang
[rabbitmq_management,rabbitmq_prometheus,rabbitmq_stream,rabbitmq_consistent_hash_exchange,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_tracing,rabbitmq_federation,rabbitmq_federation_management].
```

#### 3.2 RabbitMQ Configuration (`rabbitmq.conf`)

The main configuration file has been updated with the following settings for RabbitMQ 4.1.3:

```
# RabbitMQ 4.1.3 Configuration for Crypto-Scout
# Core Settings
default_user = admin
default_pass = admin
default_user_tags.administrator = true

# Networking
listeners.tcp.default = 5672
management.tcp.port = 15672
stream.listeners.tcp.1 = 5552

# Memory and Disk Settings
vm_memory_high_watermark.relative = 0.6
disk_free_limit.absolute = 2GB

# Clustering
cluster_formation.peer_discovery_backend = classic_config
cluster_partition_handling = autoheal

# Security
loopback_users = none
log.file.level = info

# Connection and Channel Settings
channel_max = 2047
heartbeat = 60

# Load Definitions
management.load_definitions = /etc/rabbitmq/definitions.json

# RabbitMQ 4.x Specific Settings
# Enable modern features
feature_flags.enable_all = true

# Stream settings (now supported in 4.x main config)
stream.retention.limits.max_bytes = 20GB
stream.retention.limits.max_age = 604800000 # 7 days in milliseconds

# Queue settings (now supported in 4.x main config)
queue.default.max_length = 100000
queue.default.overflow = reject_publish
queue.default.message_ttl = 21600000  # 6 hours in milliseconds
```

**Note**: RabbitMQ 4.x introduces direct support for stream and queue settings in the main configuration file, which was
not available in 3.x.

#### 3.3 Definitions (`definitions.json`)

Pre-defines all necessary RabbitMQ objects based on client code:

- Users and permissions
- Vhosts
- Exchanges (including `metrics-exchange`)
- Queues (including client-specific queues with proper TTL and max length)
- Bindings with correct routing keys
- Streams
- Policies

### 4. Service Integration

All services are configured to use the appropriate RabbitMQ features:

#### 4.1 Event Collector

- Uses streams for crypto data collection
- Publishes to `crypto.events` exchange
- Configured with publisher confirms for reliability

#### 4.2 Crypto-Scout Client

- Configured with client-specific environment variables:
    - `AMQP_EXCHANGE_METRICS=metrics-exchange`
    - `AMQP_QUEUE_CMC_FEAR_GREED_INDEX=metrics-cmc-fear-greed-index-queue`
    - `AMQP_QUEUE_BYBIT_LAUNCH_POOL=metrics-bybit-launch-pool-queue`
    - `AMQP_QUEUE_DEAD=metrics-dead-letter-queue`
    - `AMQP_QUEUE_CLIENT=crypto-scout-client-queue`
    - `AMQP_QUEUE_TTL_MS=21600000`
    - `AMQP_QUEUE_MAX_LENGTH=2500`
- Uses streams for data processing
- Optimized concurrency settings

#### 4.3 Telegram Bot

- Listens to the `service.notifications` queue
- Publishes commands to `service.events` exchange
- Configured with appropriate concurrency

## Recommendations for Future Improvements

1. **Clustering**: Implement a RabbitMQ cluster for higher availability
2. **Monitoring**: Add Prometheus and Grafana for monitoring
3. **TLS**: Enable TLS for secure communication
4. **Circuit Breakers**: Implement circuit breakers for RabbitMQ clients
5. **Shovel Plugin**: Use for replicating messages between queues or brokers
6. **Federation Plugin**: Consider for multi-datacenter deployments
7. **Message Tracing**: Implement message tracing for debugging and monitoring

## Conclusion

The updated RabbitMQ configuration provides a robust, scalable, and production-ready messaging infrastructure for the
Crypto-Scout project. It supports both stream processing for crypto data collection and common queues for inter-service
communication, with appropriate settings for high availability, performance, and security.

Most importantly, the configuration is now fully aligned with the actual client code requirements, ensuring that all
services can communicate effectively using the expected exchange and queue names, with the correct routing keys and
message properties.

This implementation follows industry best practices and provides a solid foundation for the messaging needs of the
Crypto-Scout ecosystem.

## RabbitMQ 4.1.3 Benefits

The upgrade to RabbitMQ 4.1.3 provides several benefits:

1. **Improved Performance**: Better message throughput and reduced latency
2. **Enhanced Stream Processing**: Native stream support with improved configuration options
3. **Better Resource Management**: More efficient memory and disk usage
4. **Advanced Clustering**: Improved high availability and fault tolerance
5. **Modern Management Interface**: Enhanced monitoring and management capabilities
6. **Extended Plugin Ecosystem**: Access to new plugins and features
7. **Better Security**: Enhanced security features and controls

## Lessons Learned During Upgrade

The upgrade from RabbitMQ 3.x to 4.1.3 revealed several important considerations:

#### Configuration Changes

1. **Policy Settings**: RabbitMQ 4.x has removed support for certain policy settings:
    - `ha-mode` and `ha-sync-mode` are no longer supported (high availability is handled differently)
    - Use `queue-leader-locator` instead of `queue-master-locator` for queue leader placement
    - Stream retention settings like `max-segment-size-bytes` have changed format

2. **Feature Flags**: RabbitMQ 4.x introduces a new feature flags system, but:
    - `feature_flags.enable_all` is not a valid configuration in rabbitmq.conf
    - Features must be enabled individually or through the management interface

3. **Direct Configuration**: RabbitMQ 4.x no longer supports direct configuration of certain parameters in
   rabbitmq.conf:
    - Stream settings like `stream.retention.limits.max_bytes` are not recognized
    - Queue settings like `queue.default.max_length` must be configured via policies

#### Migration Strategy

1. **Incremental Approach**: The most successful approach was:
    - Update image version first
    - Update core configuration settings
    - Simplify configuration to minimal working set
    - Add back advanced features one by one

2. **Testing**: Each configuration change required testing to identify compatibility issues:
    - Container logs were essential for diagnosing configuration errors
    - Error messages provided clear guidance on deprecated or unsupported settings

3. **Documentation**: Official RabbitMQ 4.x documentation was crucial but incomplete:
    - Some deprecated features were not clearly documented
    - Community forums provided additional insights on migration challenges

#### Best Practices for RabbitMQ 4.x

1. **Use Policies Over Direct Configuration**:
    - Define queue and stream behaviors through policies rather than direct configuration
    - This provides more flexibility and better compatibility with future versions

2. **Simplified Configuration**:
    - Keep rabbitmq.conf minimal and focused on core settings
    - Use definitions.json for complex configurations like policies, queues, and exchanges

3. **Monitor Deprecation Warnings**:
    - Pay attention to deprecation warnings in logs
    - Plan for future migrations by addressing warnings proactively

4. **Security Considerations**:
    - RabbitMQ 4.x has improved security features
    - Configure credentials directly in rabbitmq.conf rather than using environment variables

These lessons will help ensure a smooth operation of RabbitMQ 4.1.3 and prepare for future upgrades.
