# Bansaiyai API Usage Guide

This guide provides practical, verified instruction for interacting with the Bansaiyai backend API. It focuses on the core workflows verified during the Spring Boot 3.2.0 migration.

## 1. Authentication

All API endpoints (except login/register) require a JWT Bearer Token.

### Login

**Endpoint:** `POST /api/auth/login`

**Request:**

```json
{
  "username": "president",
  "password": "president123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1...",
  "type": "Bearer",
  "id": 2,
  "username": "president",
  "email": "president@bansaiyai.com",
  "roles": ["ROLE_PRESIDENT"]
}
```

**Usage:**
Include the token in the header of subsequent requests:
`Authorization: Bearer <YOUR_TOKEN>`

---

### 2.7 Payoff Calculation

**Endpoint:** `GET /loans/{id}/payoff`
**Query Params:** `date=YYYY-MM-DD` (Optional, defaults to today)
**Permissions:** `OFFICER`, `SECRETARY`, `PRESIDENT`, `MEMBER` (Own Loan)
**Response:**

```json
{
  "principal": 10000.0,
  "interest": 150.5,
  "penalty": 0.0,
  "total": 10150.5
}
```

## 3. Savings Management

**Target Role:** `PRESIDENT` or `SECRETARY`

### Create New Member

**Endpoint:** `POST /api/members`

**Required Fields:**

- `name`: 2-100 characters.
- `idCard`: Exactly 13 digits.
- `phone`: 9-10 digits.
- `address`: 10-200 characters.
- `dateOfBirth`: Prior to today.

**Verified Payload Example:**

```json
{
  "name": "Demo Member",
  "idCard": "1234567890123",
  "dateOfBirth": "1990-01-15",
  "address": "999 Peace Road, Bangkok 10110",
  "phone": "0891234567",
  "occupation": "Engineer",
  "monthlyIncome": 55000.0,
  "maritalStatus": "Single",
  "isActive": true
}
```

**Note:** `memberId` (e.g., `BSY-20251210-XXXX`) and `uuid` are auto-generated.

### Get Member Profile

**Endpoint:** `GET /api/members/{uuid}`

- **Admins/Officers**: Can view any member.
- **Members**: Can ONLY view their own profile/UUID.

---

## 3. Role Permissions

The system enforces strict RBAC.

| Role          | Capabilities       | Verified Access                                             |
| :------------ | :----------------- | :---------------------------------------------------------- |
| **PRESIDENT** | **Sole Authority** | **Loan Approvals**, Dividend Confirmation                   |
| **SECRETARY** | Accountant         | **Chart of Accounts**, Monthly Closing                      |
| **OFFICER**   | Operations         | **Member Registration**, **Receipts**, Deposits/Withdrawals |
| **MEMBER**    | Read-Only Self     | Own Profile, Own Loans, Own Savings                         |

---

## 4. Testing Tools

We have provided verified shell scripts to test the system:

### A. Create Member Demo

Simple script to login and create a random member.

```bash
./create_member_demo.sh
```

### B. Full Role API Test

Comprehensive test suite verifying all Access Control rules.

```bash
./test-roles-apis.sh
```

### C. Income/Expense Recording (New)

**Target Role:** `OFFICER`, `SECRETARY`

**Endpoint:** `POST /api/journal/entries`
**Payload:**

```json
{
  "type": "EXPENSE",
  "accountCode": "5001",
  "accountName": "Office Supplies",
  "amount": 500.0,
  "description": "Paper A4"
}
```

**Endpoint:** `GET /api/journal/summary?date=2023-10-27`
**Response:**

```json
{
  "totalIncome": 15000.0,
  "totalExpense": 500.0
}
```

## 5. System Health

- **Java Version**: 21
- **Spring Boot**: 3.2.0
- **Test Status**: 100% Pass (151/151 tests)
- **Database**: H2 In-Memory (for tests), MariaDB (production)
