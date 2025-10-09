# Context: Crypto Scout Collector ActiveJ RabbitMQ Integration

The basic implementation of the `crypto-scout-collector` service has been done in `Java 21` and `ActiveJ 6.0-rc2`,
but the integration with the `RabbitMQ` has been implemented with `Spring Boot` in `BybitQueueSubscriber.java`, 
`BybitStreamSubscriber.java`, `CmcQueueSubscriber.java`.
So let's replace the current implementation of the `BybitQueueSubscriber.java`, `BybitStreamSubscriber.java`, 
`CmcQueueSubscriber.java` in `Spring Boot` with the `reactive` implementation of `ActiveJ 6.0-rc2` that's 
`AmqpConsumer.java`. The configuration has been done in `AmqpConfig.java`. Finally write the report with your solution here
`docs/0.0.1/dev/crypto-scout-collector-activej-rabbitmq.md`.

## Roles

Take the following roles:

- Export java developer.

## Requirements

- Use the best practices and design patterns.
- Do not hallucinate.
- Rely on the existing codebase.
- Final implementation should be written in `Java 21` and `ActiveJ 6.0-rc2`.

## Tasks

- As the expert java developer develop integration between `crypto-scout-collector` and `rabbitmq` by implementing the
- reactive version `com.github.akarazhev.cryptoscout.collector.AmqpConsumer` instead of `BybitQueueSubscriber.java`, 
  `BybitStreamSubscriber.java`, `CmcQueueSubscriber.java`.
- Use `com.github.akarazhev.cryptoscout.client.AmqpPublisher`, `com.github.akarazhev.cryptoscout.config.AmqpConfig` and
  `application.properties` as samples for your implementation.
- Recheck your suggestions and make sure that they are correct, and you haven't missed any important points.
- As the expert java developer propose changes with best practices and design patterns step by step.
- Write the report with your solution: `docs/0.0.1/dev/crypto-scout-collector-activej-rabbitmq.md`.