# Context: Crypto Scout Client RabbitMQ Integration

The very basic implementation of the `crypto-scout-client` service is done in Java and Spring Boot. So you will need to
configure `RabbitMQ` integration for the `crypto-scout-client` service correctly according to the requirements and
to prepare a report with your solution.

## Roles

Take the following roles:

- Export java developer.

## Requirements

- Use the best practices and design patterns.
- Do not hallucinate.
- Rely on the existing codebase.

### RabbitMQ Configuration

- For the `metrics-exchange` exchange we will have common queues: `metrics-cmc-queue` and `metrics-bybit-queue`.
  The dead letter queue will be `metrics-dead-letter-queue`.
- For the `crypto-exchange` exchange we will have a stream: `crypto-bybit-stream`.
- For the `client-exchange` exchange we will have a queue: `crypto-scout-client-queue`.

## Tasks

- As the expert java developer develop integration between `crypto-scout-client` and `rabbitmq` by updating the
  `com.github.akarazhev.cryptoscout.config.AmqpConfig`, `application.properties` and
  `com.github.akarazhev.cryptoscout.Constants.AMQP`.
- Recheck your suggestions and make sure that they are correct and haven't missed any important points.
- As the expert java developer propose changes with best practices and design patterns step by step.
- Write the report with your solution: `docs/0.0.1/dev/crypto-scout-client-rabbitmq.md`.
- Write the specification document of your solution: `docs/0.0.1/spec/crypto-scout-client-rabbitmq.md`.
