# Scalable Tracking Number Generator API

This project implements a RESTful API using Spring Boot to generate unique tracking numbers for parcels. It's designed to be scalable, efficient, and capable of handling high concurrency.

## Features

-   **Unique Tracking Number Generation**: Uses a ID generator with Base36 encoding to produce unique, 1-16 character alphanumeric tracking numbers (`^[A-Z0-9]{1,16}$`).
-   **Database Persistence**: Stores generated tracking numbers and associated request parameters in a database (H2 in-memory by default). Uniqueness is enforced at the database level.
-   **Scalability**:
    -   The `TrackingIdGenerator` requires a unique `worker.id` per application instance to ensure global uniqueness when scaled horizontally.
    -   The application is stateless, suitable for running multiple instances behind a load balancer.
-   **API Endpoint**: `GET /api/v1/next-tracking-number`
-   **Input Validation**: Validates query parameters.
-   **API Documentation**: Includes Swagger UI for API exploration (typically at `/swagger-ui.html`).

## Requirements

-   Java 17 or later
-   Maven 3.6.x or later

## Project Structure

-   `com.example.trackingnumberapi.controller`: REST controller for API endpoints.
-   `com.example.trackingnumberapi.service`: Contains the core business logic, including `TrackingIdGenerator` and `TrackingNumberService`.
-   `com.example.trackingnumberapi.model.entity`: JPA entities.
-   `com.example.trackingnumberapi.model.repository`: Spring Data JPA repositories.
-   `com.example.trackingnumberapi.model.dto`: Data Transfer Objects for API requests/responses.
-   `com.example.trackingnumberapi.config`: Configuration classes, e.g., for explicitly defining beans like `TrackingIdGenerator`.
-   `resources/application.properties`: Application configuration.

## Setup and Running

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd tracking-number-api
    ```

2.  **Configure `tracking.worker.id` (Crucial for multiple instances):**
    Open `src/main/resources/application.properties`.
    The property `tracking.worker.id` **must be unique** for each instance of the application if you plan to run more than one for horizontal scaling.
    For local development with a single instance, the default `0` is fine. `tracking.worker.id=1` is configured by default.
    In a production environment (e.g., Docker, Kubernetes), this should be set via an environment variable:
    ```properties
    # Example for application.properties to read from environment variable
    # tracking.worker.id=${TRACKING_WORKER_ID:0}
    ```
    And then run your application instance with `TRACKING_WORKER_ID` set:
    ```bash
    export TRACKING_WORKER_ID=1 # For instance 1
    # export TRACKING_WORKER_ID=2 # For instance 2, etc.
    mvn spring-boot:run
    ```

3.  **Build the project:**
    ```bash
    mvn clean install
    ```

4.  **Run the application:**
    ```bash
    mvn spring-boot:run
    # Or run the packaged JAR:
    # java -jar target/tracking-number-api-0.0.1-SNAPSHOT.jar
    ```
    The application will start on port `8080` by default.

## Testing the API

Once the application is running:

1.  **Using `curl` or a tool like Postman/Insomnia:**

    Send a GET request to:
    `http://localhost:8080/api/v1/next-tracking-number`

    With the following query parameters (all are `required=true`):
    -   `origin_country_id` (e.g., `MY`)
    -   `destination_country_id` (e.g., `ID`)
    -   `weight` (e.g., `1.5`)
    -   `created_at` (RFC 3339 format, e.g., `2024-05-21T10:00:00+08:00`)
    -   `customer_id` (UUID, e.g., `a1b2c3d4-e5f6-7890-1234-567890abcdef`)
    -   `customer_name` (e.g., `Sample Customer`)
    -   `customer_slug` (e.g., `sample-customer`)

    **Example `curl` command:**
    ```bash
    curl --location 'http://localhost:8080/api/v1/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.5&created_at=2024-05-21T10%3A00%3A00%2B08%3A00&customer_id=a1b2c3d4-e5f6-7890-1234-567890abcdef&customer_name=Sample%20Customer&customer_slug=sample-customer'
    ```
    *(Note: The `%2B` in `created_at` is the URL-encoded form of `+`.)*

    **Expected Success Response (200 OK):**
    ```json
    {
        "trackingNumber": "YOUR_GENERATED_TRACKING_NUMBER", // Example generated tracking number
        "generatedAt": "2024-05-21T10:00:00.123456789Z" // Timestamp of generation (UTC)
    }
    ```

2.  **Using Swagger UI:**
    Open your browser and navigate to `http://localhost:8080/swagger-ui.html`.
    You can explore the API, see the model definitions, and try out the endpoint directly from the UI.

## Database

-   By default, the application uses an H2 in-memory database.
-   The H2 console is enabled and accessible at `http://localhost:8080/h2-console` during development.
    -   JDBC URL: `jdbc:h2:mem:trackingdb`
    -   User Name: `sa`
    -   Password: `password`
-   For production, configure `application.properties` to point to a persistent database (e.g., PostgreSQL, MySQL).

## Deployment

-   Package the application as a JAR: `mvn clean package`.
-   The JAR file will be in the `target/` directory (e.g., `tracking-number-api-0.0.1-SNAPSHOT.jar`).
-   Deploy this JAR to any platform that supports Java applications (e.g., AWS Elastic Beanstalk, Google App Engine, Heroku, Docker container).
-   **Remember to configure the `tracking.worker.id` (or `TRACKING_WORKER_ID` environment variable) uniquely for each deployed instance**.

## Considerations for Production

-   **Database**: Switch from H2 to a production-grade database.
-   **Worker ID Management**: Implement a robust strategy for assigning unique `tracking.worker.id` values to each application instance. This could involve environment variables set by an orchestrator (like Kubernetes), a service discovery mechanism, or a startup script that claims an ID from a central counter.
-   **Security**: Implement proper authentication and authorization if the API is exposed publicly.
-   **Logging and Monitoring**: Configure structured logging and integrate with monitoring tools.
-   **Error Handling**: Enhance global error handling and provide more specific error responses.
-   **Rate Limiting**: Implement rate limiting to protect the API from abuse.
-   **HTTPS**: Enforce HTTPS in production.