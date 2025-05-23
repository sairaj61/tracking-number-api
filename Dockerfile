# Stage 1: Build the application with JDK
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy the Maven wrapper files and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execute permissions to the Maven wrapper script
RUN chmod +x mvnw

# Copy the source code
COPY src src

# Build the application
RUN ./mvnw -B package -DskipTests

# Stage 2: Create a smaller runtime image with JRE
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR from the 'build' stage
COPY --from=build /app/target/tracking-number-api-0.0.1-SNAPSHOT.jar .

# Expose the port your application will run on
EXPOSE 8080

# Define the command to run your application
ENTRYPOINT ["java", "-jar", "tracking-number-api-0.0.1-SNAPSHOT.jar"]