# Ban Sai Yai - Savings Group Management System

**Status**: Production Ready (Backend)
**Version**: 1.0.0

A robust, enterprise-grade Spring Boot backend for managing community financial groups, featuring comprehensive role-based access, automated accounting, and strict audit logging.

## 🚀 Key Achievements

We have successfully implemented the 4 core pillars of the system:

### 1. Feature Logic (Complete)

- **Roles**: Officer, President, Secretary, Admin, Member.
- **Core Flows**:
  - **Composite Payments**: Atomic processing of Share Deposits + Loan Repayments (Interest priority).
  - **Loan Management**: Application, Approval (President), Risk Profiling.
  - **Accounting**: Monthly Closing, Dividend Calculation, Journal Entries.
  - **Reporting**: Monthly PDF Reports, Receipt Generation.

### 2. Automation (Active)

- **Dynamic Configuration**: Interest rates and system limits managed via `SystemConfig` (DB-backed).
- **Daily Jobs**: Automated scheduler runs at 00:01 AM to:
  - active **Overdue Loan Checker**: Automatically flags loans as `DEFAULTED` if maturity passed.

### 3. Security (Hardened)

- **Role-Based Access Control (RBAC)**: Strict `@PreAuthorize` on all endpoints.
- **Audit Logging**:
  - **System Config**: Tracks who changed what setting and when.
  - **Accounting**: Logs sensitive actions (Month Close, Period Confirm).
  - **Transactions**: Logs all composite payments.

### 4. Advanced Features (New)

- **Data Export**: Secretary can export Reports to CSV for analysis.
- **Demo Seeder**: `POST /api/test/seed` instantly populates the DB for testing.

### 5. Reliability (Tested)

- **Unit Testing Suite**:
  - `SystemConfigServiceTest`: Verifies config seeding and auditing.
  - `TransactionServiceTest`: Validates complex split logic (Interest vs Principal).
  - `AccountingServiceTest`: Verifies overdue logic.
  - `LoanServiceTest`: Verifies dynamic interest rates.

---

## 🛠 Tech Stack

- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 16
- **Cache**: Redis
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5 + Mockito

## 🚦 Quick Start

### Prerequisites

- Java 21
- Docker (for DB/Redis)

### Running the App

```bash
cd backend
mvn spring-boot:run
```

_Services (Postgres/Redis) will start automatically via Docker Compose._

- **API Base**: `http://localhost:9090/api`
- **Swagger UI**: [http://localhost:9090/swagger-ui.html](http://localhost:9090/swagger-ui.html)

### Running Tests

Verify the system health:

```bash
cd backend
./mvnw test
```

## 📚 Documentation

- **[Backend API Reference](file:///Users/chanthawat/.gemini/antigravity/brain/5f37f70c-fda3-4edd-a594-45dc57fa7a25/backend_api_reference.md)**: Full list of endpoints.
- **[Walkthrough](file:///Users/chanthawat/.gemini/antigravity/brain/5f37f70c-fda3-4edd-a594-45dc57fa7a25/walkthrough.md)**: Step-by-step verification guide.
