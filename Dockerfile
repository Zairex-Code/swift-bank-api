# syntax=docker/dockerfile:1

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw -B -q -DskipTests dependency:resolve

COPY src src

RUN ./mvnw -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=build /workspace/target/quarkus-app/lib/ /app/lib/
COPY --from=build /workspace/target/quarkus-app/*.jar /app/
COPY --from=build /workspace/target/quarkus-app/app/ /app/app/
COPY --from=build /workspace/target/quarkus-app/quarkus/ /app/quarkus/

USER app

EXPOSE 8080

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar"]
