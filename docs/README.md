# Ban Sai Yai Savings Group System

## Financial Accounting Information System (Spring Boot Edition)

A comprehensive financial management system for savings groups, built with Spring Boot and MariaDB. This system replaces the original PHP backend with a robust, enterprise-grade Java implementation while maintaining all business logic and functional requirements.

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- MariaDB 10.6+
- Spring Boot 4.0.0

### Setup
1. Clone the repository
2. Configure database connection in `application.properties`
3. Run `mvn spring-boot:run`
4. Access the application at `http://localhost:8080`

## 📋 Project Overview

This system implements a complete financial accounting solution for savings groups with the following key features:

- **Member Management**: Registration, profile management, and role-based access
- **Savings Management**: Share capital tracking and deposit processing
- **Loan Management**: Loan requests, approval workflow, and repayment tracking
- **Financial Reporting**: Comprehensive reports for stakeholders
- **Dividend Calculation**: Automated profit distribution calculations
- **Receipt Generation**: PDF receipts for all transactions

## 🏗️ Architecture

The system follows a layered Spring Boot architecture:

```
┌─────────────────┐
│   Controllers   │ ← HTTP Request Handling
├─────────────────┤
│    Services     │ ← Business Logic
├─────────────────┤
│  Repositories   │ ← Data Access (JPA)
├─────────────────┤
│   Database      │ ← MariaDB
└─────────────────┘
```

## 👥 User Roles & Permissions

| Role | Description | Key Permissions |
|------|-------------|-----------------|
| `ROLE_PRESIDENT` | Executive oversight | Approve loans, view dashboards |
| `ROLE_SECRETARY` | Financial management | Accounting, reports, month-end closing |
| `ROLE_OFFICER` | Daily operations | Member registration, payments, receipts |
| `ROLE_MEMBER` | Limited access | View personal data only |

## 📚 Documentation Structure

### Core Documentation
- [System Architecture](architecture/system-design.md) - Technical architecture and design patterns
- [Database Schema](architecture/database-schema.md) - Complete database structure and JPA entities
- [Security & Authentication](security/authentication-authorization.md) - Spring Security implementation
- [REST API Documentation](api/rest-endpoints.md) - Complete API reference

### Development Guides
- [Development Setup](development/setup.md) - Environment configuration
- [Sprint Implementation Plan](development/sprint-plan.md) - 8-sprint development roadmap
- [Testing Strategy](testing/unit-integration.md) - Unit and integration testing

### Sprint Documentation
1. [Sprint 1: Member Registration](sprints/sprint1-member-registration.md)
2. [Sprint 2: Savings Service](sprints/sprint2-savings-service.md)
3. [Sprint 3: Loan Management](sprints/sprint3-loan-management.md)
4. [Sprint 4: Repayment & Receipts](sprints/sprint4-repayment-receipts.md)
5. [Sprint 5: Approval Workflow](sprints/sprint5-approval-workflow.md)
6. [Sprint 6: Financial Reporting](sprints/sprint6-financial-reporting.md)
7. [Sprint 7: Dividend Calculation](sprints/sprint7-dividend-calculation.md)
8. [Sprint 8: Accounting & GL](sprints/sprint8-accounting-gl.md)

### Reference Materials
- [React Development Guide](reference/react-development-guide.md) - Comprehensive React patterns and best practices
- [React Integration Summary](reference/react-integration-summary.md) - React integration benefits and implementation roadmap
- [React Grab Integration Guide](reference/react-grab-integration.md) - Developer productivity tool for element selection and code analysis
- [JPA Entity Reference](reference/jpa-entities.md) - Detailed entity documentation
- [Business Rules](reference/business-rules.md) - Business logic and calculations
- [Data Transfer Objects](reference/data-transfer-objects.md) - DTO specifications
- [Dependency Configuration](reference/dependency-updates.md) - Maven setup

## 🛠️ Technology Stack

- **Backend**: Spring Boot 4.0.0, Spring Web MVC, Spring Security
- **Database**: MariaDB with Spring Data JPA (Hibernate)
- **Frontend**: Bootstrap + jQuery with Thymeleaf templating
- **React Frontend**: React 19.2.0 with TypeScript, Material-UI, Vite
- **Developer Tools**: React Grab for enhanced development productivity
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Reports**: PDF generation (iText/JasperReports)

## 📊 Development Progress

This project follows an Agile methodology with 8 defined sprints, each implementing a core functional module:

| Sprint | Module | Status |
|--------|--------|---------|
| 1 | Member Registration Service | 📋 Planned |
| 2 | Share & Savings Service | 📋 Planned |
| 3 | Loan Management Service | 📋 Planned |
| 4 | Repayment & Receipt Service | 📋 Planned |
| 5 | Approval Workflow Service | 📋 Planned |
| 6 | Financial Reporting Service | 📋 Planned |
| 7 | Dividend Calculation Service | 📋 Planned |
| 8 | Accounting/GL Service | 📋 Planned |

## 🚀 Deployment

- **Development**: Embedded Tomcat (Spring Boot default)
- **Production**: JAR deployment on Linux server with MariaDB
- See [Deployment Guide](deployment/production.md) for detailed instructions

## 📞 Support

For technical questions or implementation guidance, refer to the relevant documentation sections or contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: 2025  
**Framework**: Spring Boot 4.0.0 with Java 21
