# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && \
    ./gradlew --no-daemon clean bootJar -x test -x asciidoctor

WORKDIR /app
COPY --from=build /src/build/libs/*SNAPSHOT*.jar /app/app.jar

ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]