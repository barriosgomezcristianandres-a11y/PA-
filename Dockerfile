# Build stage: compile the Spring Boot application with the Java version declared in pom.xml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage: keep only the JRE and the packaged application.
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/isivi-app-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "/app/app.jar"]