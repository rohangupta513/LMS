# Loan Management System (LMS)

A full-stack Loan Management System designed to handle the complete lifecycle of loan processing—from origination and scheduling to repayments, dynamic penalties, and closure. 

## Features

- **User & Lender Management**: Manage borrowers and lenders, complete with KYC details and contact information.
- **Loan Products Configuration**: Define loan products with customizable bounds (min/max bounds for amount, interest rates, and tenure).
- **Loan Application Flow**:
  - Inquire about loans based on criteria.
  - Apply for loans and automatically generate repayment schedules (Principal + Interest).
  - Verify and approve applications.
- **Account Ledger & Settlement**:
  - Track real-time dues using the Loan Account Due ledger.
  - Process partial or full monthly credit payments.
  - Audit trail of all settlements.
- **Dynamic Penalty Calculations**:
  - Scheduled background tasks to automatically calculate Days Past Due (DPD) and apply penal charges based on configured brackets.
- **Lifecycle Actions**:
  - **Cancellation**: Users can apply to cancel loans before first payment, subject to specific conditions.
  - **Foreclosure**: Process early foreclosures with updated outstanding principal calculations.
  - **Deletion**: Full cascade-deletion available for correcting erroneous accounts safely.

## Tech Stack

### Backend
- **Java 17**
- **Spring Boot** (WebMVC, Data JPA)
- **MySQL** (Database)
- **Lombok** (Boilerplate reduction)
- **Maven** (Build Tool)

### Frontend
- **Vanilla HTML, CSS, JavaScript**
- Fully detached API-driven architecture utilizing asynchronous `fetch` calls.

## Project Structure
- `src/main/java/com/lms/backend/controller/`: REST APIs mapped to specific business domains.
- `src/main/java/com/lms/backend/service/`: Core business logic, transactional boundaries, and background `@Scheduled` tasks.
- `src/main/java/com/lms/backend/dto/`: Request and Response Data Transfer Objects ensuring a clean API contract.
- `src/main/java/com/lms/backend/entity/`: JPA entities mapping to the database tables.
- `src/main/resources/static/`: Contains the frontend UI files (`index.html`, `app.js`, `style.css`).

## Running the Application Locally

### Prerequisites
1. **Java Development Kit (JDK) 17** installed.
2. **Maven** installed.
3. **MySQL Server** running locally.

### Setup Instructions
1. **Configure Database**: Create a local MySQL database named `lms_db` (or check your `src/main/resources/application.properties` for the exact configured name and credentials).
2. **Compile the Project**:
   ```bash
   mvn clean compile
   ```
3. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
4. **Access the Frontend Dashboard**: 
   Open your browser and navigate to `http://localhost:8080` (or the configured port).

## API Documentation

All REST APIs have been decoupled from internal entities and use structured JSON Response DTOs. 

> For a complete list of endpoints and their JSON request/response structures, refer to the API Reference documentation.

Key API Domains:
- `/api/users` - User CRUD operations
- `/api/lenders` - Lender CRUD operations
- `/api/loans` - Loan Products Config
- `/api/charges` - Penalty Config
- `/api/lms` - Loan Applications, Account Status, Ledgers, and Lifecycle (Cancel/Foreclose)
- `/api/settlement` - Repayment Schedules, Credit processing, and Audits
