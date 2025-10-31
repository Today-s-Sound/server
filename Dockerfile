FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*SNAPSHOT*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
