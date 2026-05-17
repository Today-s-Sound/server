# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && \
    ./gradlew --no-daemon clean bootJar -x test -x asciidoctor

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/build/libs/*SNAPSHOT*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
