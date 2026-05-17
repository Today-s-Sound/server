# syntax=docker/dockerfile:1.7

FROM gradle:8.5-jdk17 AS build
WORKDIR /src
COPY --chown=gradle:gradle . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon clean bootJar -x test -x asciidoctor

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/build/libs/*SNAPSHOT*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
