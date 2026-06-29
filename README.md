# Team Leave Calendar with On-Call Rotation

A full-stack application for managing team leave requests and viewing a weekly on-call rotation schedule.

The application allows users to create leave requests, approve or reject them, and clearly highlights weeks where the assigned on-call person has approved leave.

## Setup Instructions

### Prerequisites

Make sure the following tools are installed:

* Docker
* Docker Compose
* Java 21
* Maven
* Node.js
* Angular CLI

The application uses:

* Backend: Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL
* Frontend: Angular 21, Angular Material
* Database: PostgreSQL

## How to Run the Application

The application can be started in two ways:

1. Running everything with Docker Compose
2. Running the database with Docker Compose and starting the backend/frontend separately

## Option 1: Run Everything with Docker Compose

From the project root, run:

```bash
docker compose up --build
```

The application will be available at:

```text
Frontend: http://localhost:4200
Backend:  http://localhost:8080
OpenAPI:  http://localhost:8080/swagger-ui/index.html
```

To stop the application:

```bash
docker compose down
```

## Option 2: Run Database, Backend and Frontend Separately

Start only the PostgreSQL database with Docker Compose:

```bash
docker compose up -d db
```

The database will be available at:

```text
Host:     localhost
Port:     5434
Database: leave_calendar_db
Username: admin
Password: admin
```

Then start the backend:

```bash
cd backend
mvn spring-boot:run
```

The backend will run on:

```text
http://localhost:8080
```

Then start the frontend:

```bash
cd frontend
npm install
ng serve
```

The frontend will run on:

```text
http://localhost:4200
```

## OpenAPI Documentation

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

## Assumptions

* The team members are fixed and predefined: Alice, Bob, Charlie and Diana.
* Leave requests are created with `PENDING` status by default.
* Leave requests can be updated to `APPROVED` or `REJECTED`.
* A team member cannot have overlapping leave requests if the existing request is `PENDING` or `APPROVED`.
* `REJECTED` leave requests are ignored when checking for overlaps.
* The on-call rotation is weekly and follows this order: Alice, Bob, Charlie, Diana.
* The on-call rotation starts from Monday, `2026-01-05`.
* The backend normalizes the requested on-call start date to the Monday of that week.
* An on-call conflict is shown only when the on-call team member has an `APPROVED` leave request overlapping that week.
* `PENDING` leave requests are not treated as on-call conflicts.

## Features Not Completed

No major requested feature was intentionally left incomplete.

The application implements:

* Leave request creation
* Leave request listing
* Filtering leave requests by team member
* Leave status updates
* Overlap prevention
* Weekly on-call rotation
* Approved leave conflict detection
* Frontend validation
* Backend validation
* OpenAPI documentation

## Optional Improvements Added

The following optional improvements were added beyond the basic requirements:

* Docker Compose setup for running the full application
* Separate development setup where only the database runs in Docker
* Swagger UI / OpenAPI documentation
* Global backend error handling with structured error responses
* Frontend date validation with date pickers
* Start date and end date validation on the frontend
* Prevention of past date selection on the frontend
* CORS configuration for local frontend-backend communication
* Unit tests for core service-level business logic
