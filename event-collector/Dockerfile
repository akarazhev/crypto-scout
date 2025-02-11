FROM amazoncorretto:23-alpine
WORKDIR /app
COPY target/event-collector-0.0.1.jar event-collector.jar
ENTRYPOINT ["java", "-jar", "event-collector.jar"]