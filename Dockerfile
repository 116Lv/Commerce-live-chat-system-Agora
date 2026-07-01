FROM gradle:9.2.1-jdk21-alpine AS build

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S agora && adduser -S agora -G agora

COPY --from=build /workspace/build/libs/*.jar app.jar

USER agora

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
