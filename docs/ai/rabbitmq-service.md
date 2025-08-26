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