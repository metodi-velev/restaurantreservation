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
- **OAuth 2.0 & OpenID Connect (Keycloak)**: Centralized IAM using Keycloak authorization server with Client Credentials grant type for machine-to-machine communication.
- **Stateless JWT Security & Role-Based Access**: Spring Security Resource Server integration with JWT and role mapping (`USER`, `ADMIN`).
- **Robust Validation**: Strict validation for party sizes, time formats, and table availability.
- **Observability**: Integrated Spring Boot Actuator for health checks and monitoring.
- **API Documentation**: Interactive Swagger/OpenAPI UI for easy testing and exploration.

---

## 🛠️ Tech Stack

- **Java 26**: Leveraging the latest modern Java features.
- **Spring Boot 4.1.0**: Core application framework.
- **Spring Security & OAuth2 Resource Server**: Stateless JWT request authorization and role mapping.
- **Keycloak 26.7.3**: Authorization server providing OAuth 2.0 and OpenID Connect IAM.
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

The application uses **Spring Security** configured with stateless **JWT (JSON Web Token)** authentication as an OAuth 2.0 Resource Server integrated with **Keycloak Authorization Server** supporting OAuth 2.0 and OpenID Connect (including machine-to-machine Client Credentials grant type).

### Authentication Flow
1. Register a new user (`POST /api/auth/register`) or use pre-configured credentials.
2. ~~Authenticate (`POST /api/auth/login`) to obtain a JWT token.~~ Clients should now authenticate directly with Keycloak.
   Start Keycloak as a docker container as described below in the section [Keycloak Authorization Server & OAuth 2.0 Setup (Client Credentials Flow)](#-keycloak-authorization-server--oauth-20-setup-client-credentials-flow) and use the `http://localhost:7080/realms/master/protocol/openid-connect/token` Keycloak endpoint to generate a new JWT token.
3. Include the JWT token in subsequent requests using the `Authorization` header:
   ```http
   Authorization: Bearer <jwt_token>
   ```

### Pre-Configured Users (Seeded on Startup)

| Username | Password | Roles | Permissions |
|---|---|---|---|
| `user` | `userpassword` | `USER` | Reserve tables, view table images, view reservations for a specific table |
| `admin` | `adminpassword` | `USER`, `ADMIN` | All user actions + Cancel reservations, view all reservations across all tables |

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
#### ⚠️ DEPRECATED — User Login (as of 2026-09-03)

> **⚠️ This endpoint is deprecated and will be removed in a future version.**  
> Please use **Keycloak's OAuth2/OIDC authentication flow** instead.  
> See the [Keycloak Authorization Server & OAuth 2.0 Setup (Client Credentials Flow)](#-keycloak-authorization-server--oauth-20-setup-client-credentials-flow) section below for details.
>
#### ~~User Login~~
~~Authenticate and obtain a JWT bearer token.~~

- **~~Endpoint~~**: ~~`POST /api/auth/login`~~ (deprecated)
- **~~Access~~**: ~~Public~~ (deprecated)
- **~~Request Headers~~**: (deprecated)
  ```http
  Content-Type: application/json
  ```
- ~~**Request Body**~~: (deprecated)
  ```json
  {
    "username": "user",
    "password": "userpassword"
  }
  ```
- **~~Response~~**: ~~`200 OK`~~ (deprecated)
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzU2NTY5NjAwLCJleHAiOjE3NTY2NTYwMDB9..."
  }
  ```
> 📌 **Migration:** Use Keycloak's token endpoint instead.

- **Request:**
```bash
POST http://localhost:7080/realms/master/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
client_id=restaurantreservation-clientid-cc
client_secret=<client-secret-copied-from-keycloak>
scope=openid email profile
```
- **Response**: `200 OK`
  ```json
  {
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkI...",
    "expires_in": 60,
    "refresh_expires_in": 0,
    "token_type": "Bearer",
    "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
    "not-before-policy": 0,
    "scope": "openid profile email"
  }
  ```
---

### 2. Table Reservation Endpoints

#### Reserve a Table
Creates a reservation by finding and assigning the smallest available table for the given party size and hourly time slot.

- **Endpoint**: `POST /tables`
- **Access**: `USER` or `ADMIN`
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
- **Access**: `USER` or `ADMIN`
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
- **Access**: `USER` or `ADMIN`
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
- **Access**: `USER` or `ADMIN`
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
- **Docker Desktop**

---

### 🔑 Keycloak Authorization Server & OAuth 2.0 Setup (Client Credentials Flow)

Follow the step-by-step guide below to start Keycloak, configure machine-to-machine OAuth 2.0 Client Credentials flow, register the client user, and verify table reservations in Postman:

1. **Start Keycloak Authorization Server**:
   Start Docker Desktop on your local machine and execute the following docker command in order to run Keycloak authorization server locally in a docker container:
   ```bash
   docker run -d -p 127.0.0.1:7080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin --name keycloak --network host --hostname=localhost quay.io/keycloak/keycloak:26.7.3 start-dev
   ```

2. **Log In to Keycloak Admin Console**:
   Once Keycloak is running, log in to its admin console on `http://localhost:7080` entering `"admin"` as username and `"admin"` as password.

3. **Create Client in Keycloak Console**:
   In the Keycloak console:
   - **3.1.** Go to **"Clients"** tab and create a new client with the client Id `"restaurantreservation-clientid-cc"`.
   - **3.2.** In the **Capability config** step of the step-wizard, toggle on **"Client authentication"** and under **"Authentication flow"** check only **"Service accounts roles"**. This is needed for the machine-to-machine **"Client Credentials"** OAuth2.0 grant type.
   - **3.3.** Click on the **"Save"** button to create the new client.

4. **Create Realm Roles**:
   Go to the tab **"Realm roles"** on the left-side menu and create the roles `"ADMIN"` and `"USER"` in the default `'master'` realm.

5. **Assign Service Account Roles**:
   Go back to the **"Clients"** menu on the left-side menu and select the newly created client `"restaurantreservation-clientid-cc"`:
   - **5.1.** Go to the **"Service accounts roles"** tab and assign `"restaurantreservation-clientid-cc"` client the newly created roles `"ADMIN"` and `"USER"`. In the filter, please select **"Filter by realm roles"** in order for the new roles to show up.

6. **Copy Client Secret**:
   Go to the **"Credentials"** tab in the client `"restaurantreservation-clientid-cc"` and copy the automatically generated client secret.

7. **Build and Run the Spring Boot App**:
   Build and run the restaurant reservation Spring Boot app:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

8. **Register Client in the Application via Postman**:
   Go to Postman and in order to create a new user with the Keycloak client Id `"restaurantreservation-clientid-cc"` and Client Secret, issue a `POST` request to the endpoint `http://localhost:8080/api/auth/register` with the following JSON body:
   ```json
   {
       "username": "restaurantreservation-clientid-cc",
       "password": "<client-secret-copied-from-keycloak>",
       "roles": ["ADMIN", "USER"]
   }
   ```

9. **Reserve a Table via Postman**:
   Try to reserve a table in Postman by issuing a `POST` request to the endpoint `http://localhost:8080/tables/with-picture`:
   - **9.1.** Enter for example the following JSON body for the `POST` request. Enter a date in the next 14 days from the start of the app. Choose `"application/json"` as a `"Content-Type"` HTTP request header:
     ```json
     {
         "timeSlotDto":
         {
             "date": "2026-09-04",
             "from": "19:00",
             "to": "21:00"
         },

         "partySize": 4
     }
     ```
   - **9.2.** Go to the authorization menu in Postman in the current request and choose **"OAuth 2.0"** as **"Auth type"** and **"Add auth data to"** with the option **"Request Headers"**.
   - **9.3.** Under the **"Configure New Token"** part in Postman Authorization enter the following settings:
     - **Token Name**: `clientcredentials_accesstoken`
     - **Grant type**: select **"Client Credentials"**
     - **Access Token URL**: `http://localhost:7080/realms/master/protocol/openid-connect/token`
     - **Client ID**: `restaurantreservation-clientid-cc`
     - **Client Secret**: `<client-secret-copied-from-keycloak>`
     - **Scope**: `openid email profile`
     - **Client Authentication**: select **"Send client credentials in body"**

10. **Obtain Token and Send Request**:
    Having done the set-up above, click on the down below orange button in Postman **"Get new access token"**:
    - **10.1.** In the following pop-up menu click on **"Proceed"** and **"Use Token"**.
    - **10.2.** Send the `POST` request by clicking on **"Send"**.

11. **Verify Successful Response**:
    You should receive HTTP status `200 OK` with the following HTTP JSON response body:
    ```json
    {
        "tableId": 1,
        "imageUrl": "http://localhost:8080/api/images/table/1"
    }
    ```

12. **Verify Second Reservation Request**:
    Try to issue one more HTTP request by clicking on **"Send"** in Postman again. You should receive the following HTTP 200 response:
    ```json
    {
        "tableId": 2,
        "imageUrl": "http://localhost:8080/api/images/table/2"
    }
    ```

---

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
