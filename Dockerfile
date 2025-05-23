# Use a base image with Java 17 JDK (for compiling)
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy the source code
COPY src src

# Build the application
# This step leverages Maven's dependency caching.
# 'mvnw -B package -DskipTests' builds the JAR and skips tests for faster deployment.
RUN ./mvnw -B package -DskipTests

# --- Optional: Multi-stage build for a smaller final image ---
# If you want a smaller final image for deployment, use a multi-stage build.
# This copies the compiled JAR from the JDK stage into a JRE stage.
# Uncomment the following lines to enable multi-stage build:

# FROM eclipse-temurin:17-jre-jammy
# WORKDIR /app
# COPY --from=0 /app/target/tracking-number-api-0.0.1-SNAPSHOT.jar .
# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "tracking-number-api-0.0.1-SNAPSHOT.jar"]

# --- If NOT using multi-stage build, keep these lines for the final image ---
# Expose the port your application will run on (default Spring Boot port)
EXPOSE 8080

# Define the command to run your application
# Assuming your JAR is named as mentioned
ENTRYPOINT ["java", "-jar", "target/tracking-number-api-0.0.1-SNAPSHOT.jar"]