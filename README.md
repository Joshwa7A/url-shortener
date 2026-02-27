# URL Shortener

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Coverage](https://img.shields.io/badge/coverage-94%25-brightgreen)

A production-ready full-stack URL Shortener application built using Spring Boot, React (Vite), MySQL, and Redis.

This project demonstrates clean architecture, caching strategy, validation, retry handling, API documentation, comprehensive testing, and high code coverage.

---

## Overview

The application provides:

- Short URL generation
- Redirection to original URLs
- Optional expiration support
- URL statistics tracking
- Redis-based caching for performance
- Controlled retry logic for robustness
- Swagger API documentation

---

## Technology Stack

### Backend
- Spring Boot
- Spring Data JPA
- MySQL
- Redis
- Swagger (OpenAPI)
- JUnit 5
- Mockito
- JaCoCo

### Frontend
- React (Vite)
- TypeScript
- Axios
- React DatePicker

### Infrastructure
- Docker
- Docker Compose

---

## Architecture

The backend follows a layered architecture:

Controller → Service → Repository

Key design principles:

- DTO-based request and response modeling
- Separation of concerns
- Centralized exception handling
- Input validation using Jakarta Bean Validation
- Redis caching to reduce database load
- Loop-based retry logic for fault tolerance
- Clean RESTful API design
- Environment-driven configuration

---

## API Endpoints

### Create Short URL
POST `/api/v1/short-urls`

### Redirect
GET `/{shortCode}`

### Get Statistics
GET `/api/v1/short-urls/{shortCode}/stats`

Swagger Documentation:
http://localhost:8081/swagger-ui.html

---

## Project Structure

```
url-shortener/
│
├── url-shortener-service/
├── url-shortener-frontend/
├── docker-compose.yml
├── .env.template
└── README.md
```

---

## Environment Configuration

- The project uses environment-based configuration.
- Create a `.env` file in the root directory using the template below.

### .env.template

```
# =========================
# Backend Configuration
# =========================

DB_URL=jdbc:mysql://mysql:3306/url_shortener_db
DB_USERNAME=your_root_name
DB_PASSWORD=your_pass

MYSQL_DATABASE=url_shortener_db
MYSQL_ROOT_PASSWORD=your_pass

REDIS_HOST=redis
REDIS_PORT=6379

ALLOWED_ORIGIN=http://localhost:3000

# =========================
# Frontend Configuration
# =========================

VITE_API_BASE_URL=http://localhost:8081
```

---

## Setup Instructions

### 1. Clone the Repository

git clone https://github.com/Joshwa7A/url-shortener.git

- cd url-shortener

---

### 2. Configure Environment Variables

- Copy the template file:
- cp .env.template .env
- Update the values in `.env` with your local database credentials.
- Ensure `.env` is excluded in `.gitignore`.

---

## Running the Application

### Option 1: Build and Run Using Docker

Make sure Docker is installed.

Run:

```
docker compose up --build
```

Access:

Backend:  
http://localhost:8081

Swagger UI:  
http://localhost:8081/swagger-ui.html

Frontend:  
http://localhost:3000

---

### Option 2: Run Using Prebuilt Docker Images

This pulls images from Docker Hub.

```
docker compose pull
docker compose up
```

---

### Option 3: Run Backend and Frontend Locally

Ensure the following services are installed and running locally:

- MySQL (8+ recommended)
- Redis (7+ recommended)
- Java 17
- Maven
- Node.js (18+ recommended)

---

### Step 1: Configure Environment Variables

Both backend and frontend use environment-based configuration.

1. Create a `.env` file in the root directory using the provided template:

   cp .env.template .env

2. Update the following values according to your local setup:

   - `DB_URL`  
     Example:  
     jdbc:mysql://localhost:3306/url_shortener_db

   - `DB_USERNAME` → Your MySQL username  
   - `DB_PASSWORD` → Your MySQL password  
   - `REDIS_HOST` → localhost  
   - `REDIS_PORT` → 6379  
   - `VITE_API_BASE_URL` → http://localhost:8081  

3. Ensure the database `url_shortener_db` exists in MySQL before starting the backend.

---

### Step 2: Start Backend

```
cd url-shortener-service  
mvn clean install  
mvn spring-boot:run
```

Backend will start at:

http://localhost:8081  

Swagger UI:

http://localhost:8081/swagger-ui.html  

---

### Step 3: Start Frontend

Open a separate terminal:

```
cd url-shortener-frontend  
npm install  
npm run dev
```

Frontend will start at:

http://localhost:5173

---

## Testing

This project includes:

- Unit tests using JUnit 5
- Mockito-based mocking for service layer testing
- Integration tests for end-to-end validation
- Exception and validation scenario coverage

To run all tests:

```
cd url-shortener-service
mvn test
```

To generate coverage report:

```
mvn clean verify
```

JaCoCo report location:

target/site/jacoco/index.html

Current Coverage: 94%+

---

## Performance and Reliability

- Redis caching reduces database load
- Controlled retry logic improves robustness
- Efficient redirect handling
- Optimized statistics retrieval

---

## Security and Best Practices

- Environment-based configuration
- Secrets not committed to repository
- Proper HTTP status codes
- Centralized exception handling
- Input validation on all endpoints
- Dockerized deployment

