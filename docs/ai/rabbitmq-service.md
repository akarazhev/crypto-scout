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

The RabbitMQ service has been upgraded to version 4.1.3 with the following configuration:

1. **Container**: Using the official RabbitMQ 4.1.3 image with management plugin
2. **Networking**: Exposing ports 5672 (AMQP), 15672 (Management UI), and 5552 (Stream)
3. **Persistence**: Volume mounted for data persistence
4. **Configuration**:
    - Core settings in rabbitmq.conf
    - Plugins in enabled_plugins
    - Definitions (users, vhosts, queues, exchanges, bindings) in definitions.json
5. **High Availability**: Configured via queue-leader-locator and queue-mode policies
6. **Security**: Admin user with password authentication
7. **Resource Limits**: Memory and disk limits configured

## Streams and Messaging Architecture

The service provides two main stream queues:

- `crypto.price.stream`: For cryptocurrency price data
- `crypto.market.stream`: For market-related events

These streams have retention policies set to 7 days.

Several classic queues are configured for different purposes:

- Metrics collection queues with overflow protection
- Client communication queues
- Service notification and command queues

## Security and High Availability

- Authentication is handled via username/password
- TLS configuration is prepared but not enabled
- High availability is configured via queue policies
- Dead letter queues are set up for failed message handling

## Upgrade Notes and Lessons Learned

The upgrade from RabbitMQ 3.x to 4.1.3 revealed several important considerations:

### Configuration Changes

1. **Policy Settings**: RabbitMQ 4.x removed support for certain policy settings:
    - `ha-mode` and `ha-sync-mode` are no longer supported
    - Use `queue-leader-locator` instead of `queue-master-locator`
    - Stream retention settings format has changed

2. **Configuration Approach**:
    - Keep rabbitmq.conf minimal with core settings only
    - Use definitions.json for complex configurations
    - Configure advanced features through policies rather than direct configuration

3. **Deprecated Features**:
    - Some features like `management_metrics_collection` are deprecated but still functional
    - Monitor logs for deprecation warnings to prepare for future upgrades

### Migration Best Practices

1. **Incremental Approach**: Update components in this order:
    - Container image version
    - Core configuration
    - Policies and definitions
    - Advanced features

2. **Testing Strategy**:
    - Check container logs for configuration errors
    - Verify service functionality after each change
    - Test all message patterns (publish/subscribe, streams, etc.)

3. **Documentation**:
    - Refer to official RabbitMQ 4.x documentation
    - Community forums provide additional migration insights

## Benefits of RabbitMQ 4.1.3

The upgrade provides several advantages:

1. **Improved Performance**: Better message throughput and reduced latency
2. **Enhanced Stream Processing**: Native stream support with improved configuration
3. **Better Resource Management**: More efficient memory and disk usage
4. **Advanced Clustering**: Improved high availability and fault tolerance
5. **Modern Management Interface**: Enhanced monitoring capabilities
6. **Extended Plugin Ecosystem**: Access to new plugins and features
7. **Better Security**: Enhanced security features and controls

A detailed report is available in `docs/dev/rabbitmq-service-report.md`.