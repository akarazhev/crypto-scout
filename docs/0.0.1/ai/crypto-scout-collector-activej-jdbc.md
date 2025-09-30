# Context: Crypto Scout Collector ActiveJ JDBC Integration

The basic implementation of the `crypto-scout-collector` service has been done in `Java 21` and `ActiveJ 6.0-rc2`,
but the integration with the `PostgreSQL` has been implemented with `Spring Boot` in `BybitServiceImpl.java`,
`CmcServiceImpl.java`. So let's replace the current implementation of the `BybitServiceImpl.java`, `CmcServiceImpl.java`
in `Spring Boot` with the `reactive` implementation of `ActiveJ 6.0-rc2` that's 
`com.github.akarazhev.cryptoscout.collector.BybitService` and `com.github.akarazhev.cryptoscout.collector.CmcService`. 
Put the configuration in `JdbcConfig.java`. Finally write the report with your solution here 
`docs/0.0.1/dev/crypto-scout-collector-activej-jdbc.md`.

## Roles

Take the following roles:

- Export java developer.

## Requirements

- Use the best practices and design patterns.
- Do not hallucinate.
- Rely on the existing codebase.
- Final implementation should be written in `Java 21` and `ActiveJ 6.0-rc2`.

## Tasks

- As the expert java developer develop integration between `crypto-scout-collector` and `PostgreSQL` by implementing the
- reactive version `com.github.akarazhev.cryptoscout.collector.BybitService` and 
  `com.github.akarazhev.cryptoscout.collector.CmcService` instead of `CmcServiceImpl.java`, `BybitServiceImpl.java`.
- Recheck your suggestions and make sure that they are correct, and you haven't missed any important points.
- As the expert java developer propose changes with best practices and design patterns step by step.
- Write the report with your solution: `docs/0.0.1/dev/crypto-scout-collector-activej-jdbc.md`.