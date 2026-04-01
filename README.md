# Mini Marketplace

A comprehensive Spring Boot based e-commerce mini marketplace application. This platform allows users to browse products, manage their own product listings, place orders, and includes an admin dashboard for centralized management.

## 🚀 Tech Stack

*   **Backend:** Java, Spring Boot, Spring Security
*   **Frontend:** Thymeleaf (HTML5, CSS)
*   **Build Tool:** Maven
*   **Containerization:** Docker, Docker Compose

## 📋 Prerequisites

Before you begin, ensure you have met the following requirements:
*   **Java Development Kit (JDK):** Version 17 or higher
*   **Maven:** For building the project locally (optional, as the Maven Wrapper is included)
*   **Docker & Docker Compose:** For running the containerized environment

## 🛠️ Configuration

The project contains an example configuration file. Before running the application locally, you should set up your environment properties.

1. Navigate to `src/main/resources/` (or the root directory if defined there).
2. Copy `application.properties.example` to `application.properties`.
3. Update the database credentials and any other environment-specific configurations in `application.properties`.

## 💻 How to Run Locally

### Option 1: Using Maven (Local Environment)

1. Open your terminal and navigate to the project root directory.
2. Clean and build the application:
   ```bash
   ./mvnw clean install
   ```
   *(On Windows, use `mvnw clean install`)*
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(On Windows, use `mvnw spring-boot:run`)*
4. Access the application in your browser at `http://localhost:8080` (or whichever port is specified in your properties file).

### Option 2: Using Docker Compose

If you prefer to run the application and its dependencies (like a database) in containers, you can use Docker Compose.

1. Open your terminal and navigate to the project root directory.
2. Build and start the containers in detached mode:
   ```bash
   docker-compose up -d --build
   ```
3. The application will be available at `http://localhost:8080`.
4. To stop the containers, run:
   ```bash
   docker-compose down
   ```

## 🧪 Testing Instructions

To run the unit and integration tests for the project, use the following Maven command:

```bash
./mvnw test
```
*(On Windows, use `mvnw test`)*

This will execute all tests located in the `src/test/java/` directory and display the test results in the console.
## 🧪 Testing using docker - 
run the following command  in terminal(Windows)  - 
docker run -it --rm -v ${PWD}:/app -w /app maven:3.9-eclipse-temurin-17 mvn test

After running the command you may see somehting named "SUCCESS" meaning the tests passed 


## 📁 Project Structure highlights

*   `src/main/java/.../controller`: Web controllers handling HTTP requests.
*   `src/main/java/.../service`: Business logic layer.
*   `src/main/java/.../repository`: Data access layer.
*   `src/main/java/.../config`: Security and application configurations.
*   `src/main/resources/templates`: Thymeleaf HTML views (Auth, Admin, Order, Product).
*   `Dockerfile` & `docker-compose.yml`: Containerization configuration.

