# 🍽️ Restaurant Reservation System

A professional, high-performance Spring Boot application designed to manage restaurant table reservations with precision and efficiency. This system ensures optimal table utilization by automatically assigning the smallest available table that fits the party size while adhering to strict business rules.

---

## 🚀 Features

- **Table Reservation**: Reserve tables by specifying party size and a desired hourly time slot.
- **Intelligent Allocation**: Automatically assigns the smallest available table that accommodates the party.
- **Reservation with Picture**: Reserve a table and receive an endpoint URL to view the table's photo.
- **Reservation Queries**: Retrieve all reservations across the restaurant (Admin) or for specific table IDs.
- **Reservation Cancellation**: Free up reserved tables for specific time slots.
- **Image Service**: Serve table pictures dynamically via dedicated endpoints.
- **Time Slot Management**: Supports hourly slots (e.g., 10:00–11:00, 18:00–19:00) up to 14 days in advance.
- **Concurrency & Reliability**: Handled via JPA optimistic locking (`@Version`) combined with Spring Retry (`@Retryable`) with exponential backoff.
- **Stateless JWT Security**: Full Spring Security integration using JSON Web Tokens (JWT) and role-based access control (`ROLE_USER`, `ROLE_ADMIN`).
- **Robust Validation**: Strict validation for party sizes, time formats, and table availability.
- **Observability**: Integrated Spring Boot Actuator for health checks and monitoring.
- **API Documentation**: Interactive Swagger/OpenAPI UI for easy testing and exploration.

---

## 🛠️ Tech Stack

- **Java 26**: Leveraging the latest modern Java features.
- **Spring Boot 4.1.0**: Core application framework.
- **Spring Security**: Robust authentication and role-based access control.
- **JSON Web Token (JJWT 0.11.5)**: Stateless token-based security and request authorization.
- **Spring Data JPA & Hibernate**: ORM and database management with optimistic locking.
- **Spring Retry & Spring Aspects**: Retry mechanism with backoff for concurrent access handling.
- **H2 Database**: High-performance in-memory database with web console.
- **Caffeine Cache**: High-performance in-memory caching.
- **Jakarta Validation**: Bean validation ensuring data integrity across requests.
- **SpringDoc OpenAPI 3.0.0**: Automated OpenAPI 3 specification and Swagger UI.
- **Lombok**: Boilerplate reduction.
- **Testcontainers & PostgreSQL**: Realistic database testing.

---

## 📋 Business Rules

- **Fixed Inventory**: The restaurant operates with 10 tables of varying capacities:
  - Table 1: 4 seats
  - Table 2: 6 seats
  - Table 3: 8 seats
  - Table 4: 12 seats
  - Table 5: 16 seats
  - Table 6: 20 seats
  - Table 7: 26 seats
  - Table 8: 30 seats
  - Table 9: 36 seats
  - Table 10: 40 seats
- **Single Occupancy**: Each table can have only one active reservation per hourly slot.
- **Hourly Slots Only**: Reservations must align with full hours (e.g., `18:00:00` to `19:00:00`).
- **Operating Hours**: The restaurant operates daily from **10:00 to 23:00**.
- **Booking Window**: Reservations can be made up to **14 days** in advance.
- **Optimal Allocation**: Always prioritizes the smallest table that can fit the party to maximize seating capacity.

---

## 🔐 Security & Authentication

The application uses **Spring Security** configured with stateless **JWT (JSON Web Token)** authentication.

### Authentication Flow
1. Register a new user (`POST /api/auth/register`) or use pre-configured credentials.
2. Authenticate (`POST /api/auth/login`) to obtain a JWT token.
3. Include the JWT token in subsequent requests using the `Authorization` header:
   ```http
   Authorization: Bearer <jwt_token>
   ```

### Pre-Configured Users (Seeded on Startup)

| Username | Password | Roles | Permissions |
|---|---|---|---|
| `user` | `userpassword` | `ROLE_USER` | Reserve tables, view table images, view reservations for a specific table |
| `admin` | `adminpassword` | `ROLE_USER`, `ROLE_ADMIN` | All user actions + Cancel reservations, view all reservations across all tables |

### Role-Based Access Matrix

| Endpoint | HTTP Method | Required Role / Access |
|---|---|---|
| `/api/auth/**` | POST | Public (Permit All) |
| `/api/public/**` | ANY | Public (Permit All) |
| `/swagger-ui/**`, `/v3/api-docs/**` | GET | Public (Permit All) |
| `/h2-console/**` | ANY | Public (Permit All) |
| `/tables` | POST | `USER`, `ADMIN` |
| `/tables/with-picture` | POST | `USER`, `ADMIN` |
| `/tables/reservations/{tableId}` | GET | `USER`, `ADMIN` |
| `/api/images/**` | GET | `USER`, `ADMIN` |
| `/tables/{tableId}` | DELETE | `ADMIN` only |
| `/tables/reservations` | GET | `ADMIN` only |

---

## 🔌 API Documentation & Sample Requests / Responses

### 1. Authentication Endpoints

#### User Registration
Register a new user account with optional roles (defaults to none or specify roles such as `USER`, `ADMIN`).

- **Endpoint**: `POST /api/auth/register`
- **Access**: Public
- **Request Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "username": "johndoe",
    "password": "password123",
    "roles": ["USER"]
  }
  ```
- **Response**: `200 OK`
  ```text
  User registered successfully
  ```

---

#### User Login
Authenticate and obtain a JWT bearer token.

- **Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "username": "user",
    "password": "userpassword"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzU2NTY5NjAwLCJleHAiOjE3NTY2NTYwMDB9..."
  }
  ```

---

### 2. Table Reservation Endpoints

#### Reserve a Table
Creates a reservation by finding and assigning the smallest available table for the given party size and hourly time slot.

- **Endpoint**: `POST /tables`
- **Access**: `ROLE_USER` or `ROLE_ADMIN`
- **Request Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <jwt_token>
  ```
- **Request Body**:
  ```json
  {
    "partySize": 4,
    "timeSlotDto": {
      "date": "2026-08-30",
      "from": "18:00:00",
      "to": "19:00:00"
    }
  }
  ```
- **Response**: `200 OK`
  ```json
  1
  ```
  *(Returns the unique `tableId` assigned)*

---

#### Reserve a Table (With Picture URL)
Creates a reservation and returns both the assigned table ID and an image endpoint URL to view the table picture.

- **Endpoint**: `POST /tables/with-picture`
- **Access**: `ROLE_USER` or `ROLE_ADMIN`
- **Request Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <jwt_token>
  ```
- **Request Body**:
  ```json
  {
    "partySize": 6,
    "timeSlotDto": {
      "date": "2026-08-30",
      "from": "19:00:00",
      "to": "20:00:00"
    }
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "tableId": 2,
    "imageUrl": "http://localhost:8080/api/images/table/2"
  }
  ```

---

#### Get All Reservations (Admin Only)
Retrieve all active reservations across all tables in the restaurant.

- **Endpoint**: `GET /tables/reservations`
- **Access**: `ROLE_ADMIN` only
- **Request Headers**:
  ```http
  Authorization: Bearer <jwt_admin_token>
  ```
- **Response**: `200 OK`
  ```json
  [
    {
      "tableId": 1,
      "date": "2026-08-30",
      "fromTime": "18:00:00",
      "toTime": "19:00:00"
    },
    {
      "tableId": 2,
      "date": "2026-08-30",
      "fromTime": "19:00:00",
      "toTime": "20:00:00"
    }
  ]
  ```

---

#### Get Reservations for a Specific Table
Retrieve all reservations associated with a particular table ID.

- **Endpoint**: `GET /tables/reservations/{tableId}`
- **Access**: `ROLE_USER` or `ROLE_ADMIN`
- **Path Parameter**: `tableId` (e.g. `1`)
- **Request Headers**:
  ```http
  Authorization: Bearer <jwt_token>
  ```
- **Response**: `200 OK`
  ```json
  [
    {
      "tableId": 1,
      "date": "2026-08-30",
      "fromTime": "18:00:00",
      "toTime": "19:00:00"
    }
  ]
  ```

---

#### Cancel a Reservation
Cancels an existing reservation for a specified table and time slot.

- **Endpoint**: `DELETE /tables/{tableId}`
- **Access**: `ROLE_ADMIN` only
- **Path Parameter**: `tableId` (e.g. `1`)
- **Request Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <jwt_admin_token>
  ```
- **Request Body**:
  ```json
  {
    "timeSlotDto": {
      "date": "2026-08-30",
      "from": "18:00:00",
      "to": "19:00:00"
    },
    "partySize": 4
  }
  ```
- **Response**: `204 No Content`

---

### 3. Image Endpoints

#### Get Table Image
Fetches the JPEG image binary associated with the given table ID.

- **Endpoint**: `GET /api/images/table/{tableId}`
- **Access**: `ROLE_USER` or `ROLE_ADMIN`
- **Path Parameter**: `tableId` (e.g. `1`)
- **Request Headers**:
  ```http
  Authorization: Bearer <jwt_token>
  ```
- **Response**: `200 OK`
  - **Content-Type**: `image/jpeg`
  - **Body**: Binary image JPEG data

---

### 4. Error Responses

When an error occurs (validation error, resource not found, or rule violation), the API returns a structured error object:

```json
{
  "code": "INVALID_PARTY_SIZE",
  "message": "Party size must be between 1 and 40.",
  "status": 400
}
```

---

## ⚡ Concurrency & Optimistic Locking

To guarantee consistency during high-load and concurrent reservation requests:
- Tables utilize JPA `@Version` column for **Optimistic Locking**.
- Service operations are decorated with `@Retryable` to handle `OptimisticLockingFailureException`, `TimeSlotAlreadyReservedException`, and `TimeSlotNotFoundException`.
- Failed attempts automatically retry up to **5 times** with exponential backoff (`delay = 50ms, multiplier = 1.5`) before delegating to `@Recover` handler methods.

---

## ⚙️ Setup & Execution

### Prerequisites
- **JDK 26**
- **Maven 3.x**

### Running the Application
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd restaurantreservation
   ```
2. Build the project:
   ```bash
   ./mvnw clean install
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Interactive API Documentation (Swagger / OpenAPI)
Access the interactive Swagger UI once the application is running:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Database Console
Access the in-memory H2 database console:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: *(leave blank)*

### 📊 Health & Metrics Monitoring
Spring Boot Actuator endpoints:
- **Health Check**: `http://localhost:8080/actuator/health`
- **Actuator Root**: `http://localhost:8080/actuator`

---
*Developed as a high-quality Spring Boot example demonstrating best practices in API design, JWT security, and concurrency handling.*
