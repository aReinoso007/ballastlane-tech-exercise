# syntax=docker/dockerfile:1

# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Resolve dependencies first so this layer is cached until the build files change.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
RUN useradd --system --create-home app
WORKDIR /app
COPY --from=build /workspace/build/libs/app.jar app.jar
USER app

ENV SERVER_PORT=8089
EXPOSE 8089
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=6 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
