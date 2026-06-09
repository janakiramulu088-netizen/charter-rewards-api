# Customer Rewards API

A production-quality Spring Boot RESTful API for calculating customer reward points earned through purchase transactions.

## Overview

This application implements a customer rewards program that awards points based on purchase amounts:
- **2 points** per dollar spent **over $100** per transaction
- **1 point** per dollar spent **between $50 and $100** per transaction
- **0 points** for amounts **$50 or below**

Example: A $120 purchase earns 2×$20 + 1×$50 = **90 points**



---

## Quick Start

### Prerequisites

- **Java 17** or later
- **Maven 3.8+**
- Git

### Building

```bash
# Clone the repository
git clone https://github.com/yourusername/rewards-api.git
cd rewards-api

# Build the project
mvn clean package
```

### Running

```bash
# Run the application
mvn spring-boot:run
```

The application starts on **http://localhost:8080**

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

---

## Project Structure

```
rewards-api/
├── src/main/java/com/charter/rewards/
│   ├── RewardsApiApplication.java          # Spring Boot entry point
│   ├── controller/
│   │   └── RewardsController.java          # REST endpoints
│   ├── service/
│   │   ├── RewardsService.java             # Business logic interface
│   │   ├── RewardsServiceImpl.java          # Service implementation
│   │   └── TransactionDataService.java     # In-memory sample data
│   ├── model/
│   │   └── Transaction.java                # Domain model
│   ├── dto/
│   │   ├── CustomerRewardSummaryDto.java   # Customer summary
│   │   ├── MonthlyRewardDto.java           # Monthly breakdown
│   │   ├── RewardsResponseDto.java         # Response wrapper
│   │   └── ErrorResponseDto.java           # Error envelope
│   ├── exception/
│   │   ├── CustomerNotFoundException.java   # 404 exception
│   │   ├── InvalidDateRangeException.java   # 400 exception
│   │   └── GlobalExceptionHandler.java     # Centralized error handling
│   └── config/
│       └── OpenApiConfig.java              # Swagger/OpenAPI config
│
├── src/main/resources/
│   └── application.properties               # Spring Boot configuration
│
├── src/test/java/com/charter/rewards/
│   ├── service/
│   │   └── RewardsServiceTest.java         # Unit tests (30+ test cases)
│   └── controller/
│       └── RewardsControllerTest.java      # Integration tests (20+ test cases)
│
├── pom.xml                                  # Maven dependencies
└── README.md                                # This file
```

### Key Classes

#### **RewardsController**
REST endpoints for retrieving reward summaries. Provides three main endpoints:
- `GET /api/v1/rewards` – Rewards for all customers
- `GET /api/v1/rewards/{customerId}` – Rewards for a specific customer
- `GET /api/v1/rewards/calculate` – Ad-hoc points calculation

#### **RewardsService / RewardsServiceImpl**
Core business logic:
- Point calculation per transaction
- Monthly aggregation per customer
- Date-range filtering and validation

#### **TransactionDataService**
Provides a curated in-memory dataset with:
- **5 customers** (C001–C005)
- **24 transactions** across January–March 2024
- Varied amounts (below $50, $50–$100, over $100) for comprehensive testing

#### **GlobalExceptionHandler**
Centralised exception mapping:
- `CustomerNotFoundException` → 404
- `InvalidDateRangeException` → 400
- Validation errors → 400 with field details
- Generic exceptions → 500

---

## API Endpoints

All dates use **ISO 8601 format** (e.g., `2024-01-15`).

### 1. Get All Customer Rewards

```http
GET /api/v1/rewards?startDate=2024-01-01&endDate=2024-03-31
```

**Query Parameters:**
- `startDate` (optional): Inclusive start date. Default: 3 months before today
- `endDate` (optional): Inclusive end date. Default: today

**Response:**
```json
{
  "period": "2024-01 to 2024-03",
  "customers": [
    {
      "customerId": "C001",
      "customerName": "Alice Johnson",
      "monthlyRewards": [
        {
          "year": 2024,
          "month": 1,
          "monthName": "JANUARY",
          "points": 95
        },
        ...
      ],
      "totalPoints": 475
    },
    ...
  ],
  "totalTransactions": 24
}
```

**Status Codes:**
- `200 OK` – Success
- `400 Bad Request` – Invalid date range

---

### 2. Get Customer Rewards

```http
GET /api/v1/rewards/C001?startDate=2024-01-01&endDate=2024-03-31
```

**Path Parameters:**
- `customerId` (required): Unique customer identifier

**Query Parameters:** (same as endpoint #1)

**Response:**
```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "monthlyRewards": [
    {
      "year": 2024,
      "month": 1,
      "monthName": "JANUARY",
      "points": 95
    },
    ...
  ],
  "totalPoints": 475
}
```

**Status Codes:**
- `200 OK` – Success
- `404 Not Found` – Customer does not exist
- `400 Bad Request` – Invalid date range

---

### 3. Calculate Points for Amount

```http
GET /api/v1/rewards/calculate?amount=120.00
```

**Query Parameters:**
- `amount` (required): Transaction amount in USD (must be > 0)

**Response:**
```
90
```

**Status Codes:**
- `200 OK` – Success (returns long value)
- `400 Bad Request` – Invalid amount

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RewardsServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage

#### **RewardsServiceTest** (30+ test cases)
- Point calculation for various amounts
- Floor logic for fractional cents
- Customer lookups and summaries
- Date-range filtering
- Null/invalid date handling
- Monthly aggregation

#### **RewardsControllerTest** (20+ test cases)
- HTTP status codes (200, 400, 404)
- Response structure and field validation
- Date parameter parsing
- Error response format
- Edge cases (empty datasets, future dates, single-day ranges)
- Negative test scenarios




## Implementation Details

### Points Calculation Algorithm

```
FUNCTION calculatePoints(amount)
  IF amount <= $50:
    RETURN 0
  ELSE IF amount <= $100:
    RETURN floor(amount) - 50   // 1 point per dollar in this band
  ELSE:
    RETURN 50 + 2 * (floor(amount) - 100)  // 50 for $50-$100 band
                                            // 2 points per dollar over $100
```

**Key Rules:**
- Only the **integer (floor) portion** is used when applying thresholds
- Each transaction is calculated independently
- Monthly totals sum all transactions in that month
- Overall total sums all monthly totals

### Date Range Handling

- **Inclusive**: Start and end dates are both inclusive
- **No hardcoded months**: Month names derived from `java.time.Month` enum
- **Validation**: Start date must not exceed end date; both must be non-null
- **Default window**: If not specified, defaults to 3-month rolling window ending today

### Database/Data Strategy

This version uses an **in-memory dataset** provided by `TransactionDataService`:
- Suitable for demos, prototypes, and assignment submission
- Can be replaced with a JPA/Hibernate layer pointing to a persistent database (e.g., PostgreSQL)
- Minimal schema change required: `TransactionDataService` injects data from external source

---

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.2.5 | REST framework |
| Java | 17 | Language |
| Lombok | Latest | Boilerplate reduction |
| SpringDoc OpenAPI | 2.5.0 | Swagger UI / API docs |
| JUnit 5 | Latest | Unit testing |
| Spring Test | 3.2.5 | Integration testing |
| Maven | 3.8+ | Build tool |

---

## Code Quality

### Java Standards Applied


### Best Practices

- Single Responsibility Principle: Each class has one clear purpose
- Dependency Injection: Spring handles all bean wiring
- DTOs for API contracts: Loose coupling between internal and external representations
- Service layer: Business logic isolated from HTTP concerns
- Comprehensive testing: 50+ test cases with good coverage

---

## Sample Data

The `TransactionDataService` provides 24 sample transactions:

| Customer | Name | # Txns | Months | Sample Amounts |
|----------|------|--------|--------|----------------|
| C001 | Alice Johnson | 6 | Jan–Mar | $120, $75.50, $200, $45, $130.75, $88 |
| C002 | Bob Smith | 5 | Jan–Mar | $55, $110, $95, $150, $30 |
| C003 | Carol White | 5 | Jan–Mar | $250, $60, $40, $175, $99.99 |
| C004 | David Brown | 4 | Jan–Mar | $500, $350, $85, $420 |
| C005 | Eva Martinez | 4 | Jan–Mar | $25, $72, $105, $49.99 |

This diversity exercises all point-calculation tiers:
- Amounts below $50 → 0 points
- Amounts $50–$100 → 1 point per dollar
- Amounts over $100 → 2 points per dollar + $50 band

---

## Building for Production

### Compile and Package

```bash
mvn clean package -DskipTests
```

Creates `target/rewards-api-1.0.0.jar`

### Run JAR

```bash
java -jar target/rewards-api-1.0.0.jar
```

### Docker

```dockerfile
FROM openjdk:17-slim
COPY target/rewards-api-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

---





**Version:** 1.0.0  
**Last Updated:** June 2024  
**Author:** Charter Assignment Submission
