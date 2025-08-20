# Crypto Scout Code Review Report

This is a code review report for the Crypto Scout project.

## Crypto Scout Client

### High-impact issues

- Bug: messages published to default exchange. [+]
- Config values are defined but unused. [+/-]: Wire the API key from env (never empty string).
- Stream resilience: no retries/resubscription. [+]

### Architecture and design

- Clear separation of concerns is good. [+]
- Package structure. [+]
- Use of RxJava in Spring Boot 3 [?]

### Reliability, messaging, and backpressure

- Publisher confirms and returns. [-]
- Consumer converter for JSON. [+]
- Queue declarations and ownership. [+]
- Threading and schedulers. [-]

### Observability and ops

- Actuator and metrics. [-]
- Structured logging. [-]
- Graceful shutdown. [-]

### Configuration and security

- API key handling. [-]
- Rabbit credentials. [-]
- Prefer YAML and profiles. [-]

### Build, runtime, and containerization

- Java version. [-]
- Dockerization. [+]
- Dependency hygiene. [-]

### Testing

- Unit tests. [-]
- Integration tests. [-]

### Small cleanups

- Simplify routing logic. [+]
- Property-driven TTLs. [+]
- Visibility and package. [+]