FROM amazoncorretto:23-alpine
WORKDIR /app
COPY target/cex-client-0.0.1.jar cex-client.jar
ENTRYPOINT ["java", "-jar", "cex-client.jar"]