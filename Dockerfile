# Use a multi-stage build to create a lean final image.

# --- Build Stage ---
# Use a Gradle image with JDK 21 to build the application.
FROM gradle:8.8-jdk21-jammy AS builder

# Set the working directory inside the container.
WORKDIR /workspace/app

# Copy the build configuration file.
COPY build.gradle.kts ./

# Copy the source code.
COPY src ./src

# Build the application, creating an executable JAR.
# The --no-daemon flag is recommended for CI/CD environments.
RUN gradle bootJar --no-daemon

# --- Runtime Stage ---
# Use a minimal JRE image for the final application container.
# Amazon Corretto is a good choice for AWS environments like EC2.
FROM amazoncorretto:21-alpine

# Set the working directory for the application.
WORKDIR /app

# Copy the executable JAR from the build stage to the runtime container.
# The JAR is renamed to app.jar for simplicity.
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Expose the port on which the Spring Boot application listens.
EXPOSE 8080

# Define the command to run the application.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
