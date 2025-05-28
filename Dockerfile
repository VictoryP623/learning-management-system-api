# Build stage: dùng Maven build JAR
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage: dùng JAR vừa build ra
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Giới hạn RAM JVM tối đa 256MB
CMD ["java", "-Xmx512m", "-XX:+UseContainerSupport", "-jar", "app.jar"]

