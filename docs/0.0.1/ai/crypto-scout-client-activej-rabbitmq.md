# Context: Crypto Scout Client ActiveJ RabbitMQ Integration

The basic implementation of the `crypto-scout-client` service has been done in `Java 21` and `ActiveJ 6.0-rc2`, 
but the integration with the `RabbitMQ` has been implemented with `Spring Boot`. So let's replace the current 
implementation of the `DataPublisher.java` in `Spring Boot` with the implementation of `ActiveJ 6.0-rc2` that's 
`AmqpClient.java`. The configuration has been done in `AmqpConfig.java`. Finally write the report with your 
solution here `docs/0.0.1/dev/crypto-scout-client-activej-rabbitmq.md`.

## Roles

Take the following roles:

- Export java developer.

## Requirements

- Use the best practices and design patterns.
- Do not hallucinate.
- Rely on the existing codebase.
- Final implementation should be written in `Java 21` and `ActiveJ 6.0-rc2`.

## Tasks

- As the expert java developer develop integration between `crypto-scout-client` and `rabbitmq` by updating the
  `com.github.akarazhev.cryptoscout.config.AmqpClient`.
- Use `com.github.akarazhev.cryptoscout.amqp.DataPublisher`, `com.github.akarazhev.cryptoscout.config.AmqpConfig` and 
- `application.properties` as the base for your implementation.
- Recheck your suggestions and make sure that they are correct and you haven't missed any important points.
- As the expert java developer propose changes with best practices and design patterns step by step.
- Write the report with your solution: `docs/0.0.1/dev/crypto-scout-client-activej-rabbitmq.md`.