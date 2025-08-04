# Первый этап
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Второй этап
FROM eclipse-temurin:17-jre-jammy
LABEL authors="Semenikhin A.F"
WORKDIR /app
COPY --from=build /app/target/*.jar my-app.jar
COPY resources ./resources
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "my-app.jar"]





