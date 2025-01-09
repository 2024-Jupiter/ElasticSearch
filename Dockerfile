FROM openjdk:17-jdk
COPY build/libs/*SNAPSHOT.jar app.jar
COPY src/main/resources/application-local.yaml application-local.yaml
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local", "/app.jar"]