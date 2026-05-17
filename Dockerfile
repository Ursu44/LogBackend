FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app
COPY . /app

RUN mvn clean package -DskipTests -B

EXPOSE 8080
CMD ["java", "-Xmx512m", "-jar", "target/LogsBackend-0.0.1-SNAPSHOT.jar"]