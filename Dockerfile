# Step 1: Build the JAR using Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the JAR
FROM openjdk:17-jdk-slim
WORKDIR /app
# This grabs the .jar file Maven just created in the target folder
COPY --from=build /app/target/*.jar app.jar

# Google Cloud Run specifically listens on port 8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]