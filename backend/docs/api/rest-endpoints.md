# REST API Documentation

## Overview

The Ban Sai Yai Savings Group system provides a comprehensive REST API for all system operations. The API follows RESTful principles with JSON request/response format and JWT-based authentication.

## Base URL
```
Development: http://localhost:8080/api
Production: https://bansaiyai.example.com/api
```

## Authentication

All API endpoints (except authentication endpoints) require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Response Format

### Success Response
```json
{
    "success": true,
    "data": {
        // Response data
    },
    "message": "Operation completed successfully",
    "timestamp": "2025-01-15T10:30:00Z"
}
```

### Error Response
```json
{
    "success": false,
    "error": {
        "code": "VALIDATION_ERROR",
        "message": "Invalid input data",
        "details": [
            {
                "field": "amount",
                "message": "Amount must be greater than 0"
            }
        ]
    },
    "timestamp": "2025-01-15T10:30:00Z"
}
```

## API Endpoints

### Authentication Endpoints

#### POST /api/auth/login
Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
    "username": "john.doe",
    "password": "securePassword123"
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9...",
        "username": "john.doe",
        "role": "ROLE_OFFICER",
        "expiresIn": 86400000
    },
    "message": "Authentication successful"
}
```

**Status Codes:**
- `200 OK` - Authentication successful
- `401 Unauthorized` - Invalid credentials
- `400 Bad Request` - Invalid request format

#### POST /api/auth/refresh
Refreshes an existing JWT token.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
    "success": true,
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9...",
        "expiresIn": 86400000
    }
}
```

### Member Management Endpoints

#### POST /api/members/register
Registers a new member in the system.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "name": "Jane Smith",
    "idCard": "1234567890123",
    "address": "123 Main St, Bangkok",
    "username": "jane.smith",
    "password": "tempPassword123",
    "role": "ROLE_MEMBER",
    "photoBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..."
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "memberId": 123,
        "name": "Jane Smith",
        "idCard": "1234567890123",
        "dateRegist": "2025-01-15",
        "username": "jane.smith",
        "role": "ROLE_MEMBER"
    },
    "message": "Member registered successfully"
}
```

#### GET /api/members/{id}
Retrieves member details by ID.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT` or member viewing own data

**Path Parameters:**
- `id` (Long) - Member ID

**Response:**
```json
{
    "success": true,
    "data": {
        "memberId": 123,
        "name": "Jane Smith",
        "idCard": "1234567890123",
        "address": "123 Main St, Bangkok",
        "dateRegist": "2025-01-15",
        "photoPath": "/uploads/members/123.jpg",
        "savingAccount": {
            "savingId": 456,
            "shareCapital": 10000.00,
            "deposit": 5000.00,
            "balance": 15000.00
        },
        "user": {
            "username": "jane.smith",
            "role": "ROLE_MEMBER"
        }
    }
}
```

#### GET /api/members/me
Retrieves current user's member profile.

**Required Roles:** `ROLE_MEMBER`

**Response:** Same format as `/api/members/{id}`

#### GET /api/members
Retrieves all members (paginated).

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Query Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `search` (String, optional) - Search term for name or ID card

**Response:**
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "memberId": 123,
                "name": "Jane Smith",
                "idCard": "1234567890123",
                "dateRegist": "2025-01-15"
            }
        ],
        "pageable": {
            "page": 0,
            "size": 20,
            "totalElements": 150,
            "totalPages": 8
        }
    }
}
```

### Loan Management Endpoints

#### POST /api/loans/apply
Submits a new loan application.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "memberId": 123,
    "amount": 50000.00,
    "interestRate": 12.5,
    "loanType": "PERSONAL",
    "purpose": "Business expansion",
    "termMonths": 12,
    "collateral": {
        "type": "PROPERTY",
        "description": "Land title deed",
        "documentRef": "LD-2025-001"
    },
    "guarantors": [124, 125]
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "loanId": 789,
        "memberId": 123,
        "amount": 50000.00,
        "interestRate": 12.5,
        "loanType": "PERSONAL",
        "status": "PENDING",
        "applicationDate": "2025-01-15",
        "guarantors": [
            {
                "memberId": 124,
                "name": "John Doe"
            },
            {
                "memberId": 125,
                "name": "Mary Johnson"
            }
        ]
    },
    "message": "Loan application submitted successfully"
}
```

#### PUT /api/loans/{id}/approve
Approves a pending loan application.

**Required Roles:** `ROLE_PRESIDENT`

**Path Parameters:**
- `id` (Long) - Loan ID

**Request Body:**
```json
{
    "approvedBy": "president.user",
    "approvalNotes": "Approved for business expansion"
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "loanId": 789,
        "status": "APPROVED",
        "approvalDate": "2025-01-15",
        "approvedBy": "president.user",
        "approvalNotes": "Approved for business expansion"
    },
    "message": "Loan approved successfully"
}
```

#### PUT /api/loans/{id}/reject
Rejects a pending loan application.

**Required Roles:** `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "rejectedBy": "president.user",
    "rejectionReason": "Insufficient collateral"
}
```

#### GET /api/loans/{id}
Retrieves loan details by ID.

**Required Roles:** All roles (with appropriate access restrictions)

**Response:**
```json
{
    "success": true,
    "data": {
        "loanId": 789,
        "member": {
            "memberId": 123,
            "name": "Jane Smith"
        },
        "amount": 50000.00,
        "interestRate": 12.5,
        "loanType": "PERSONAL",
        "status": "ACTIVE",
        "applicationDate": "2025-01-15",
        "approvalDate": "2025-01-15",
        "currentBalance": 45000.00,
        "guarantors": [
            {
                "memberId": 124,
                "name": "John Doe"
            }
        ]
    }
}
```

#### GET /api/loans/my-loans
Retrieves current user's loans.

**Required Roles:** `ROLE_MEMBER`

**Query Parameters:**
- `status` (String, optional) - Filter by loan status

#### GET /api/loans
Retrieves all loans (paginated).

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Query Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `status` (String, optional) - Filter by status
- `memberId` (Long, optional) - Filter by member

### Savings Management Endpoints

#### POST /api/savings/deposit
Processes a savings deposit.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "memberId": 123,
    "amount": 5000.00,
    "depositType": "SHARE_CAPITAL",
    "description": "Monthly share contribution",
    "transactionDate": "2025-01-15"
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "transactionId": "TXN-2025-001",
        "memberId": 123,
        "amount": 5000.00,
        "depositType": "SHARE_CAPITAL",
        "newBalance": 20000.00,
        "transactionDate": "2025-01-15",
        "receiptUrl": "/api/receipts/TXN-2025-001"
    },
    "message": "Deposit processed successfully"
}
```

#### POST /api/savings/withdrawal
Processes a savings withdrawal.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "memberId": 123,
    "amount": 2000.00,
    "reason": "Emergency withdrawal",
    "transactionDate": "2025-01-15"
}
```

#### GET /api/savings/my-balance
Retrieves current user's savings balance.

**Required Roles:** `ROLE_MEMBER`

**Response:**
```json
{
    "success": true,
    "data": {
        "memberId": 123,
        "shareCapital": 15000.00,
        "deposit": 5000.00,
        "totalBalance": 20000.00,
        "lastUpdated": "2025-01-15T10:30:00Z"
    }
}
```

### Payment Management Endpoints

#### POST /api/payments/process
Processes a loan repayment.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "loanId": 789,
    "amount": 5000.00,
    "paymentDate": "2025-01-15",
    "paymentMethod": "CASH",
    "reference": "Payment receipt #123"
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "paymentId": "PAY-2025-001",
        "loanId": 789,
        "amount": 5000.00,
        "principalPaid": 4000.00,
        "interestPaid": 1000.00,
        "remainingBalance": 40000.00,
        "paymentDate": "2025-01-15",
        "receiptUrl": "/api/receipts/PAY-2025-001"
    },
    "message": "Payment processed successfully"
}
```

#### GET /api/payments/loan/{loanId}
Retrieves payment history for a specific loan.

**Required Roles:** Based on loan ownership or admin roles

### Financial Reporting Endpoints

#### GET /api/reports/financial/balance-sheet
Generates balance sheet report.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Query Parameters:**
- `asOfDate` (String, format: YYYY-MM-DD) - Report date

**Response:**
```json
{
    "success": true,
    "data": {
        "reportDate": "2025-01-15",
        "assets": {
            "currentAssets": 1500000.00,
            "fixedAssets": 500000.00,
            "totalAssets": 2000000.00
        },
        "liabilities": {
            "currentLiabilities": 800000.00,
            "longTermLiabilities": 200000.00,
            "totalLiabilities": 1000000.00
        },
        "equity": {
            "shareCapital": 800000.00,
            "retainedEarnings": 200000.00,
            "totalEquity": 1000000.00
        }
    }
}
```

#### GET /api/reports/financial/profit-loss
Generates profit and loss statement.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Query Parameters:**
- `startDate` (String, format: YYYY-MM-DD) - Period start
- `endDate` (String, format: YYYY-MM-DD) - Period end

#### GET /api/reports/financial/loan-portfolio
Generates loan portfolio report.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

### Dividend Management Endpoints

#### POST /api/dividends/calculate
Calculates dividends for all members.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "calculationDate": "2025-01-15",
    "dividendRates": {
        "shareRate": 0.08,
        "depositRate": 0.06
    },
    "totalProfit": 500000.00
}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "calculationId": "DIV-2025-001",
        "totalMembers": 150,
        "totalDividends": 400000.00,
        "averageDividend": 2666.67,
        "dividendDetails": [
            {
                "memberId": 123,
                "memberName": "Jane Smith",
                "shareDividend": 1200.00,
                "depositDividend": 300.00,
                "totalDividend": 1500.00
            }
        ]
    },
    "message": "Dividends calculated successfully"
}
```

#### GET /api/dividends/member/{memberId}
Retrieves dividend history for a member.

**Required Roles:** `ROLE_MEMBER` (own data) or admin roles

### Accounting Management Endpoints

#### POST /api/accounting/close-month
Performs month-end closing procedures.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "closingDate": "2025-01-31",
    "createdBy": "secretary.user"
}
```

#### GET /api/accounting/chart-of-accounts
Retrieves chart of accounts.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

#### POST /api/accounting/journal-entry
Creates a manual journal entry.

**Required Roles:** `ROLE_SECRETARY`, `ROLE_PRESIDENT`

**Request Body:**
```json
{
    "transactionDate": "2025-01-15",
    "description": "Manual adjustment",
    "entries": [
        {
            "accountCode": "1001",
            "debitAmount": 1000.00,
            "creditAmount": 0.00
        },
        {
            "accountCode": "2001",
            "debitAmount": 0.00,
            "creditAmount": 1000.00
        }
    ]
}
```

### Receipt Generation Endpoints

#### GET /api/receipts/{receiptId}
Downloads a receipt PDF.

**Required Roles:** Based on receipt ownership or admin roles

**Path Parameters:**
- `receiptId` (String) - Receipt ID

**Query Parameters:**
- `format` (String, default: PDF) - Receipt format (PDF/HTML)

**Response:** Binary PDF data or error JSON

### Dashboard Endpoints

#### GET /api/dashboard/executive
Retrieves executive dashboard data.

**Required Roles:** `ROLE_PRESIDENT`

**Response:**
```json
{
    "success": true,
    "data": {
        "totalMembers": 150,
        "activeLoans": 45,
        "totalSavings": 2500000.00,
        "outstandingLoans": 1800000.00,
        "monthlyRevenue": 150000.00,
        "pendingLoans": 8,
        "overdueLoans": 3,
        "portfolioHealth": "GOOD",
        "recentTransactions": [
            {
                "type": "LOAN_PAYMENT",
                "amount": 5000.00,
                "date": "2025-01-15",
                "memberName": "Jane Smith"
            }
        ]
    }
}
```

#### GET /api/dashboard/officer
Retrieves officer dashboard data.

**Required Roles:** `ROLE_OFFICER`, `ROLE_SECRETARY`

#### GET /api/dashboard/member
Retrieves member dashboard data.

**Required Roles:** `ROLE_MEMBER`

## Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource already exists |
| `BUSINESS_RULE_VIOLATION` | 422 | Business rule violation |
| `INTERNAL_ERROR` | 500 | Internal server error |

## Rate Limiting

API endpoints are rate-limited to prevent abuse:

- **Authentication endpoints**: 5 requests per minute
- **Data modification endpoints**: 100 requests per minute
- **Read-only endpoints**: 1000 requests per minute

## Pagination

Paginated endpoints use the following standard format:

```json
{
    "content": [...],
    "pageable": {
        "page": 0,
        "size": 20,
        "totalElements": 150,
        "totalPages": 8,
        "first": true,
        "last": false
    }
}
```

**Query Parameters:**
- `page` (int, default: 0) - Zero-based page index
- `size` (int, default: 20) - Page size (max: 100)
- `sort` (String) - Sorting field and direction (e.g., "name,desc")

## Search and Filtering

Many endpoints support search and filtering:

```json
GET /api/members?search=jane&status=ACTIVE&page=0&size=10
```

**Common Parameters:**
- `search` (String) - General search term
- `status` (String) - Filter by status
- `dateFrom` (String, YYYY-MM-DD) - Filter by date range
- `dateTo` (String, YYYY-MM-DD) - Filter by date range

## File Uploads

For endpoints that accept file uploads:

```http
POST /api/members/{id}/upload-photo
Content-Type: multipart/form-data

photo: [binary file data]
```

**Supported Formats:**
- Images: JPEG, PNG (max 5MB)
- Documents: PDF (max 10MB)

## API Versioning

The current API version is **v1**. Version is specified in the URL:

```
/api/v1/members
```

Future versions will maintain backward compatibility where possible.

## Testing

Use the following curl examples for testing:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Get members (with token)
curl -X GET http://localhost:8080/api/members \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

**Related Documentation**:
- [Security Implementation](../security/authentication-authorization.md) - Authentication details
- [System Design](../architecture/system-design.md) - API architecture
- [Testing Strategy](../testing/unit-integration.md) - API testing approaches
