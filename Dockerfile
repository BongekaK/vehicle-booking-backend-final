# Stage 1: Build the application
FROM gradle:8.5.0-jdk17 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test

# Stage 2: Create the final image
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Use CMD to allow Heroku to pass arguments if needed
CMD ["java", "-jar", "app.jar"]
