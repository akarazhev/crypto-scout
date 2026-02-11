---
name: maven-build
description: Maven build configuration for the crypto-scout multi-module Java 25 project
license: MIT
compatibility: opencode
metadata:
  tool: maven
  language: java
  version: "25"
---

## What I Do

Provide guidance for building, testing, and packaging the crypto-scout multi-module Maven project.

## Project Structure

```
crypto-scout/
├── pom.xml                      # Root aggregator POM (version 0.0.1)
│   └── modules:
│       ├── jcryptolib (0.0.4)   # Library JAR
│       ├── crypto-scout-test (0.0.1)    # Test library JAR
│       ├── crypto-scout-client (0.0.1)  # Fat JAR service
│       ├── crypto-scout-collector (0.0.1) # Fat JAR service
│       └── crypto-scout-analyst (0.0.1)   # Fat JAR service
```

Note: `crypto-scout-mq` is infrastructure-only and not a Java/Maven module.

## Build Commands

### Full Build
```bash
# Clean and build all modules
mvn clean install

# Build without tests (faster)
mvn -q -DskipTests install
```

### Module-Specific Builds
```bash
# Build specific module
cd crypto-scout-client
mvn clean package

# Build with dependency resolution from root
cd crypto-scout-collector
mvn clean package -DskipTests
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
cd crypto-scout-test && mvn test

# Run single test class
mvn test -Dtest=AmqpPublisherTest

# Run single test method
mvn test -Dtest=AmqpPublisherTest#shouldPublishPayloadToStream

# Run with extended timeout (slow environments)
mvn -q -Dpodman.compose.up.timeout.min=5 test

# Custom database URL for tests
mvn -q -Dtest.db.jdbc.url=jdbc:postgresql://localhost:5432/crypto_scout test
```

### Clean Build
```bash
# Clean all modules
mvn clean

# Clean and rebuild single module
cd crypto-scout-client && mvn clean package -DskipTests
```

## POM Configuration

### Root POM (Aggregator)
```xml
<project>
    <groupId>com.github.akarazhev.cryptoscout</groupId>
    <artifactId>crypto-scout</artifactId>
    <version>0.0.1</version>
    <packaging>pom</packaging>

    <modules>
        <module>jcryptolib</module>
        <module>crypto-scout-test</module>
        <module>crypto-scout-client</module>
        <module>crypto-scout-collector</module>
        <module>crypto-scout-analyst</module>
    </modules>
</project>
```

### Library POM (jcryptolib, crypto-scout-test)
```xml
<project>
    <parent>
        <groupId>com.github.akarazhev.cryptoscout</groupId>
        <artifactId>crypto-scout</artifactId>
        <version>0.0.1</version>
    </parent>

    <artifactId>jcryptolib</artifactId>
    <version>0.0.4</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>25</java.version>
        <maven.compiler.release>25</maven.compiler.release>
        <activej.version>6.0-rc2</activej.version>
        <dsl-json.version>2.0.2</dsl-json.version>
        <ta4j-core.version>0.22.1</ta4j-core.version>
        <junit-jupiter.version>6.1.0-M1</junit-jupiter.version>
    </properties>

    <build>
        <plugins>
            <!-- Compiler plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.0</version>
                <configuration>
                    <release>25</release>
                </configuration>
            </plugin>

            <!-- Jar plugin for libraries -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### Service POM (client, collector, analyst)
```xml
<project>
    <parent>
        <groupId>com.github.akarazhev.cryptoscout</groupId>
        <artifactId>crypto-scout</artifactId>
        <version>0.0.1</version>
    </parent>

    <artifactId>crypto-scout-client</artifactId>
    <packaging>jar</packaging>

    <properties>
        <java.version>25</java.version>
        <maven.compiler.release>25</maven.compiler.release>
        <maven.shade.plugin.version>3.6.1</maven.shade.plugin.version>
        <activej.version>6.0-rc2</activej.version>
        <jcryptolib.version>0.0.4</jcryptolib.version>
    </properties>

    <build>
        <plugins>
            <!-- Compiler plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.1</version>
                <configuration>
                    <release>25</release>
                </configuration>
            </plugin>

            <!-- Shade plugin for fat JAR -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.6.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.github.akarazhev.cryptoscout.Client</mainClass>
                                </transformer>
                            </transformers>
                            <createDependencyReducedPom>true</createDependencyReducedPom>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## Key Dependencies by Module

### jcryptolib (0.0.4)
| Dependency | Version | Purpose |
|------------|---------|---------|
| activej-datastream | 6.0-rc2 | Async data streams |
| activej-http | 6.0-rc2 | HTTP client/server |
| dsl-json | 2.0.2 | JSON serialization |
| ta4j-core | 0.22.1 | Technical analysis |
| slf4j-api | 2.1.0-alpha1 | Logging API |
| logback-classic | 1.5.27 | Logging backend |
| junit-jupiter | 6.1.0-M1 | Testing |
| mockito | 5.21.0 | Mocking |

### crypto-scout-test (0.0.1)
| Dependency | Version | Purpose |
|------------|---------|---------|
| jcryptolib | 0.0.4 | JSON utilities |
| junit-jupiter | 6.1.0-M1 | JUnit 6 testing |
| stream-client | 1.4.0 | RabbitMQ Streams |
| amqp-client | 5.28.0 | RabbitMQ AMQP |
| postgresql | 42.7.9 | PostgreSQL driver |

### crypto-scout-client (0.0.1)
| Dependency | Version | Purpose |
|------------|---------|---------|
| jcryptolib | 0.0.4 | JSON utilities, clients |
| activej-servicegraph | 6.0-rc2 | DI and lifecycle |
| activej-jmx | 6.0-rc2 | JMX monitoring |
| stream-client | 1.4.0 | RabbitMQ Streams |

### crypto-scout-collector (0.0.1)
| Dependency | Version | Purpose |
|------------|---------|---------|
| jcryptolib | 0.0.4 | JSON utilities |
| activej-servicegraph | 6.0-rc2 | DI and lifecycle |
| activej-jmx | 6.0-rc2 | JMX monitoring |
| activej-datastream | 6.0-rc2 | Data streaming |
| stream-client | 1.4.0 | RabbitMQ Streams |
| amqp-client | 5.28.0 | RabbitMQ AMQP |
| postgresql | 42.7.9 | PostgreSQL driver |
| HikariCP | 7.0.2 | Connection pooling |
| crypto-scout-test | 0.0.1 | Test utilities |

### crypto-scout-analyst (0.0.1)
| Dependency | Version | Purpose |
|------------|---------|---------|
| Same as crypto-scout-collector |

## Build Artifacts

### Output Locations
| Module | Artifact | Type | Location |
|--------|----------|------|----------|
| jcryptolib | JAR library | Library | `target/jcryptolib-0.0.4.jar` |
| crypto-scout-test | JAR library | Library | `target/crypto-scout-test-0.0.1.jar` |
| crypto-scout-client | Fat JAR | Service | `target/crypto-scout-client-0.0.1.jar` |
| crypto-scout-collector | Fat JAR | Service | `target/crypto-scout-collector-0.0.1.jar` |
| crypto-scout-analyst | Fat JAR | Service | `target/crypto-scout-analyst-0.0.1.jar` |

### Running Fat JARs
```bash
# crypto-scout-client
java -jar crypto-scout-client/target/crypto-scout-client-0.0.1.jar

# crypto-scout-collector
java -jar crypto-scout-collector/target/crypto-scout-collector-0.0.1.jar

# crypto-scout-analyst
java -jar crypto-scout-analyst/target/crypto-scout-analyst-0.0.1.jar
```

## Troubleshooting

### Build Failures
```bash
# Clear local repo and rebuild
rm -rf ~/.m2/repository/com/github/akarazhev
mvn clean install

# Debug dependency tree
mvn dependency:tree

# Check for updates
mvn versions:display-dependency-updates
```

### Test Failures
```bash
# Run with verbose output
mvn test -X

# Skip tests temporarily
mvn install -DskipTests

# Run specific test with debug
mvn test -Dtest=ClassName -Dmaven.surefire.debug
```

## When to Use Me

Use this skill when:
- Building the project for the first time
- Running tests across modules
- Creating new module POMs
- Troubleshooting build failures
- Configuring Maven plugins
- Understanding dependency management
- Packaging services for deployment
- Managing version updates
