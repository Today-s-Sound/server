FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*SNAPSHOT*.jar /app/app.jar

ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# 💡 수정된 부분: sh -c 를 사용해 JAVA_OPTS 변수를 주입합니다.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]