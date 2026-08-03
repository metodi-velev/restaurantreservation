# 🍽️ Restaurant Reservation System

A professional, high-performance Spring Boot application designed to manage restaurant table reservations with precision and efficiency. This system ensures optimal table utilization by automatically assigning the smallest available table that fits the party size while adhering to strict business rules.

## 🚀 Features

-   **Table Reservation**: Reserve tables by specifying party size and a desired hourly time slot.
-   **Intelligent Allocation**: Automatically assigns the smallest available table that accommodates the party.
-   **Time Slot Management**: Supports hourly slots (e.g., 18:00, 19:00, 20:00) up to 14 days in advance.
-   **Reservation Cancellation**: Ability to free up tables for specific time slots.
-   **Robust Validation**: Strict validation for party sizes, time formats, and availability.
-   **Observability**: Integrated Spring Boot Actuator for health checks and monitoring.
-   **API Documentation**: Interactive Swagger/OpenAPI UI for easy testing.

## 🛠️ Tech Stack

-   **Java 26**: Leveraging the latest language features.
-   **Spring Boot 4.1.0**: The core framework for the application.
-   **Spring Data JPA**: For seamless database interaction.
-   **H2 Database**: High-performance in-memory database for rapid development and testing.
-   **Lombok**: To reduce boilerplate code.
-   **Jakarta Validation**: Ensuring data integrity across all layers.
-   **SpringDoc OpenAPI**: Automatic generation of API documentation.
-   **Testcontainers**: For reliable integration testing with PostgreSQL.

## 📋 Business Rules

-   **Fixed Inventory**: The restaurant operates with 10 tables of varying capacities:
    -   4, 6, 8, 12, 16, 20, 26, 30, 36, and 40 seats.
-   **Single Occupancy**: Each table can have only one reservation per hourly slot.
-   **Hourly Only**: Reservations are strictly hourly (e.g., 10:00-11:00, 11:00-12:00).
-   **Operating Hours**: The restaurant is open from **10:00 to 23:00**.
-   **Optimization**: Always prioritize the smallest table that fits the party to maximize restaurant capacity.

## 🔌 API Documentation

### Reserve a Table
`POST /tables`

**Request Body:**
```json
{
  "partySize": 4,
  "timeSlotDto": {
    "date": "2026-08-05",
    "from": "18:00:00",
    "to": "19:00:00"
  }
}
```

**Response:**
Returns the reserved `tableId` (Long).

### API Explorer
Once the application is running, you can access the interactive Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## ⚙️ Setup & Execution

### Prerequisites
-   JDK 26
-   Maven 3.x

### Running the Application
1. Clone the repository.
2. Build the project:
   ```bash
   ./mvnw clean install
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Database Console
Access the H2 In-Memory database console at:
`http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User**: `sa`
- **Password**: (blank)

## 📊 Monitoring
Health and metrics endpoints are available via Spring Boot Actuator:
-   **Health Check**: `http://localhost:8080/actuator/health`
-   **All Endpoints**: `http://localhost:8080/actuator`

---
*Developed as a high-quality Spring Boot example demonstrating best practices in API design and business logic implementation.*
