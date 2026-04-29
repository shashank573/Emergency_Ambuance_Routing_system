# Use an official Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use an official OpenJDK image to run the app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/emergency-ambulance-routing-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
