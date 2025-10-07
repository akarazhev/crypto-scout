# Context: Crypto Scout Collector ActiveJ JDBC Connection Pool Integration

The basic implementation of the `crypto-scout-collector` service has been done in `Java 21` and `ActiveJ 6.0-rc2`,
but the integration with the `PostgreSQL` has been implemented with the simple `PGSimpleDataSource` in `JdbcConfig.java`.
So let's replace the current implementation of the `Datasource`, with the highly concurrent and efficient connection pool 
that's `hikaricp`. Put the configuration in `JdbcConfig.java` and `application.properties`. 

## Roles

Take the following roles:

- Export java developer.

## Requirements

- Use the best practices and design patterns.
- Do not hallucinate.
- Rely on the existing codebase.
- Use the latest version of the technical stack..

## Tasks

- As the expert java developer develop integration of the `hikaricp` connection pool in `crypto-scout-collector` service.
- Replace the current implementation of the `Datasource` with the `hikaricp` connection pool.
- Add the `hikaricp` connection pool configuration to the `application.properties` and `JdbcConfig.java`.
- Recheck your suggestions and proposals and make sure that they are correct, and you haven't missed any important points.
- As the expert java developer propose changes with best practices and design patterns step by step.