# Backend API Endpoints Reference

> **Base URL**: `http://localhost:8080`
>
> **Note**: Replace `{id}`, `{username}`, etc. with actual values when testing.
> Most endpoints require authentication. Add `-H "Authorization: Bearer YOUR_TOKEN"` to authenticated requests.

## Table of Contents

- [Authentication](#authentication)
- [Audit](#audit)
- [Cash Reconciliation](#cash-reconciliation)
- [Dashboard](#dashboard)
- [Guarantor](#guarantor)
- [Loans](#loans)
- [Members](#members)
- [Payments](#payments)
- [Receipts](#receipts)
- [Roles](#roles)
- [Savings](#savings)
- [Users](#users)

---

## Authentication

**Base Path**: `/auth`

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password","rememberMe":false}'
```

### Register

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"user@example.com","password":"password"}'
```

### Logout

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"token":"YOUR_REFRESH_TOKEN"}'
```

### Get Current User

```bash
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Check Username Availability

```bash
curl -X GET http://localhost:8080/auth/check-username/testuser
```

### Check Email Availability

```bash
curl -X GET http://localhost:8080/auth/check-email/test@example.com
```

### Forgot Password

```bash
curl -X POST http://localhost:8080/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

### Reset Password

```bash
curl -X POST http://localhost:8080/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"RESET_TOKEN","newPassword":"newpass","confirmPassword":"newpass"}'
```

---

## Audit

**Base Path**: `/api/audit`
**Required Role**: PRESIDENT

### Get Critical Actions

```bash
curl -X GET http://localhost:8080/api/audit/critical-actions \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Role Violations

```bash
curl -X GET http://localhost:8080/api/audit/role-violations \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Activity Heatmap

```bash
curl -X GET "http://localhost:8080/api/audit/activity-heatmap?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Off-Hours Alerts

```bash
curl -X GET "http://localhost:8080/api/audit/off-hours-alerts?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Audit Summary

```bash
curl -X GET "http://localhost:8080/api/audit/summary?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Cash Reconciliation

**Base Path**: `/api/cash-reconciliation`

### Create Reconciliation

**Required Role**: OFFICER

```bash
curl -X POST http://localhost:8080/api/cash-reconciliation \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"physicalCount":50000.00,"notes":"End of day count"}'
```

### Get Pending Reconciliations

**Required Role**: SECRETARY

```bash
curl -X GET http://localhost:8080/api/cash-reconciliation/pending \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Approve Discrepancy

**Required Role**: SECRETARY

```bash
curl -X POST http://localhost:8080/api/cash-reconciliation/1/approve \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"action":"APPROVE","notes":"Approved after verification"}'
```

### Reject Discrepancy

**Required Role**: SECRETARY

```bash
curl -X POST http://localhost:8080/api/cash-reconciliation/1/reject \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"action":"REJECT","notes":"Discrepancy too large"}'
```

### Check Can Close Day

```bash
curl -X GET http://localhost:8080/api/cash-reconciliation/can-close-day \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Reconciliation by ID

```bash
curl -X GET http://localhost:8080/api/cash-reconciliation/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Dashboard

**Base Path**: `/api/dashboard`

### Get Current Fiscal Period

```bash
curl -X GET http://localhost:8080/api/dashboard/fiscal-period
```

### Search Members

```bash
curl -X GET "http://localhost:8080/api/dashboard/members/search?q=john&limit=5"
```

### Get Cash Box Tally

**Required Role**: OFFICER, PRESIDENT

```bash
curl -X GET http://localhost:8080/api/dashboard/officer/cash-box \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Recent Transactions

**Required Role**: OFFICER

```bash
curl -X GET "http://localhost:8080/api/dashboard/officer/recent-transactions?limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Member Financials

**Required Role**: OFFICER, SECRETARY

```bash
curl -X GET http://localhost:8080/api/dashboard/members/1/financials \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Trial Balance

**Required Role**: SECRETARY

```bash
curl -X GET http://localhost:8080/api/dashboard/secretary/trial-balance \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Unclassified Count

**Required Role**: SECRETARY

```bash
curl -X GET http://localhost:8080/api/dashboard/secretary/unclassified-count \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Financial Previews

**Required Role**: SECRETARY

```bash
curl -X GET http://localhost:8080/api/dashboard/secretary/financial-previews \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get PAR Analysis

**Required Role**: PRESIDENT

```bash
curl -X GET http://localhost:8080/api/dashboard/president/par-analysis \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get PAR Details

**Required Role**: PRESIDENT

```bash
curl -X GET "http://localhost:8080/api/dashboard/president/par-details?category=30_DAYS" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Liquidity Ratio

**Required Role**: PRESIDENT

```bash
curl -X GET http://localhost:8080/api/dashboard/president/liquidity \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Membership Trends

**Required Role**: PRESIDENT

```bash
curl -X GET "http://localhost:8080/api/dashboard/president/membership-trends?months=12" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Member Passbook

**Required Role**: MEMBER

```bash
curl -X GET http://localhost:8080/api/dashboard/member/passbook \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loan Obligation

**Required Role**: MEMBER

```bash
curl -X GET http://localhost:8080/api/dashboard/member/loan-obligation \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Dividend Estimate

**Required Role**: MEMBER

```bash
curl -X GET http://localhost:8080/api/dashboard/member/dividend-estimate \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Process Deposit

```bash
curl -X POST http://localhost:8080/api/dashboard/transactions/deposit \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"amount":1000.00,"description":"Monthly deposit"}'
```

### Process Loan Payment

```bash
curl -X POST http://localhost:8080/api/dashboard/transactions/loan-payment \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"loanId":1,"amount":500.00,"description":"Monthly payment"}'
```

### Get Minimum Interest

```bash
curl -X GET http://localhost:8080/api/dashboard/loans/1/minimum-interest \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Guarantor

**Base Path**: `/api`

### Get Guaranteed Loans

```bash
curl -X GET http://localhost:8080/api/members/1/guaranteed-loans \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loan by ID (with Guarantor Access)

```bash
curl -X GET http://localhost:8080/api/loans/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Loans

**Base Path**: `/loans`

### Apply for Loan

```bash
curl -X POST http://localhost:8080/loans/apply \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"loanType":"PERSONAL","amount":10000.00,"purpose":"Home repair","guarantors":[2,3]}'
```

### Approve Loan

```bash
curl -X POST http://localhost:8080/loans/1/approve \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"approvedAmount":10000.00,"interestRate":5.0,"termMonths":12}'
```

### Disburse Loan

```bash
curl -X POST http://localhost:8080/loans/1/disburse \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Reject Loan

```bash
curl -X POST "http://localhost:8080/loans/1/reject?rejectionReason=Insufficient+collateral" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get All Loans

```bash
curl -X GET "http://localhost:8080/loans?page=0&size=10&sort=id,desc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loan by ID

```bash
curl -X GET http://localhost:8080/loans/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loan by Number

```bash
curl -X GET http://localhost:8080/loans/number/LN-2024-001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loans by Member

```bash
curl -X GET "http://localhost:8080/loans/member/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loans by Status

```bash
curl -X GET "http://localhost:8080/loans/status/ACTIVE?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loans by Type

```bash
curl -X GET "http://localhost:8080/loans/type/PERSONAL?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Search Loans

```bash
curl -X GET "http://localhost:8080/loans/search?keyword=john&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update Loan

```bash
curl -X PUT http://localhost:8080/loans/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"loanType":"PERSONAL","amount":12000.00,"purpose":"Updated purpose"}'
```

### Delete Loan

```bash
curl -X DELETE http://localhost:8080/loans/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Loan Statistics

```bash
curl -X GET http://localhost:8080/loans/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Check Loan Eligibility

```bash
curl -X GET "http://localhost:8080/loans/eligibility/1?requestedAmount=15000.00" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Pending Loans

```bash
curl -X GET "http://localhost:8080/loans/pending?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Active Loans

```bash
curl -X GET "http://localhost:8080/loans/active?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Members

**Base Path**: `/members`

### Get All Members

```bash
curl -X GET "http://localhost:8080/members?page=0&size=10&sortBy=id&sortDir=desc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Member by ID

```bash
curl -X GET http://localhost:8080/members/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get My Profile

```bash
curl -X GET http://localhost:8080/members/my-profile \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create Member

```bash
curl -X POST http://localhost:8080/members \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"0812345678"}'
```

### Update Member

```bash
curl -X PUT http://localhost:8080/members/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","phone":"0812345678"}'
```

### Delete Member

```bash
curl -X DELETE http://localhost:8080/members/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Search Members

```bash
curl -X GET "http://localhost:8080/members/search?keyword=john" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Active Members

```bash
curl -X GET http://localhost:8080/members/active \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Inactive Members

```bash
curl -X GET http://localhost:8080/members/inactive \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Member Statistics

```bash
curl -X GET http://localhost:8080/members/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Payments

**Base Path**: `/api/payments`

### Create Payment

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"amount":500.00,"paymentType":"LOAN_PAYMENT","loanId":1}'
```

### Get Payment by ID

```bash
curl -X GET http://localhost:8080/api/payments/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Payment by Number

```bash
curl -X GET http://localhost:8080/api/payments/by-number/PAY-2024-001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Payments by Member

```bash
curl -X GET "http://localhost:8080/api/payments/member/1?page=0&size=10&sortBy=paymentDate&sortDir=desc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Payments by Loan

```bash
curl -X GET "http://localhost:8080/api/payments/loan/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Payments by Savings Account

```bash
curl -X GET "http://localhost:8080/api/payments/savings/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get All Payments

```bash
curl -X GET "http://localhost:8080/api/payments?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Process Payment

```bash
curl -X POST http://localhost:8080/api/payments/1/process \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Verify Payment

```bash
curl -X POST http://localhost:8080/api/payments/1/verify \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Cancel Payment

```bash
curl -X POST "http://localhost:8080/api/payments/1/cancel?reason=Duplicate+payment" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Overdue Payments

```bash
curl -X GET http://localhost:8080/api/payments/overdue \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Pending Payments

```bash
curl -X GET http://localhost:8080/api/payments/pending \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Payment Statistics

```bash
curl -X GET "http://localhost:8080/api/payments/statistics?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Payments by Date Range

```bash
curl -X GET "http://localhost:8080/api/payments/by-date-range?startDate=2024-01-01&endDate=2024-12-31&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Member Payment Summary

```bash
curl -X GET http://localhost:8080/api/payments/member/1/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Search Payments

```bash
curl -X GET "http://localhost:8080/api/payments/search?query=john&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Receipts

**Base Path**: `/api/receipts`

### Get Receipt PDF

```bash
curl -X GET http://localhost:8080/api/receipts/1/pdf \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output receipt-1.pdf
```

---

## Roles

**Base Path**: `/api/roles`

### Get All Roles

```bash
curl -X GET http://localhost:8080/api/roles \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Role Hierarchy

```bash
curl -X GET http://localhost:8080/api/roles/hierarchy \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Role Permissions

```bash
curl -X GET http://localhost:8080/api/roles/OFFICER/permissions \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get All Permissions

```bash
curl -X GET http://localhost:8080/api/roles/permissions \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Roles by Permission

```bash
curl -X GET http://localhost:8080/api/roles/by-permission/VIEW_MEMBERS \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Check Can Manage Role

```bash
curl -X GET http://localhost:8080/api/roles/PRESIDENT/can-manage/OFFICER \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Validate Role Assignment

```bash
curl -X POST http://localhost:8080/api/roles/validate-assignment \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetRole":"OFFICER"}'
```

### Get Role Permissions by ID

```bash
curl -X GET http://localhost:8080/api/roles/1/permissions \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Add Permission to Role

```bash
curl -X POST http://localhost:8080/api/roles/1/permissions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"permissionSlug":"VIEW_REPORTS"}'
```

### Remove Permission from Role

```bash
curl -X DELETE http://localhost:8080/api/roles/1/permissions/5 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Role Statistics

```bash
curl -X GET http://localhost:8080/api/roles/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Savings

**Base Path**: `/savings`

### Create Savings Account

```bash
curl -X POST http://localhost:8080/savings/accounts \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"accountType":"REGULAR","initialDeposit":1000.00}'
```

### Get Account by ID

```bash
curl -X GET http://localhost:8080/savings/accounts/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Account by Number

```bash
curl -X GET http://localhost:8080/savings/accounts/by-number/SAV-2024-001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Accounts by Member

```bash
curl -X GET "http://localhost:8080/savings/accounts/member/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get All Accounts

```bash
curl -X GET "http://localhost:8080/savings/accounts?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Deposit to Account

```bash
curl -X POST "http://localhost:8080/savings/accounts/1/deposit?amount=500.00&description=Monthly+deposit" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Withdraw from Account

```bash
curl -X POST "http://localhost:8080/savings/accounts/1/withdraw?amount=200.00&description=Withdrawal" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Freeze Account

```bash
curl -X POST "http://localhost:8080/savings/accounts/1/freeze?reason=Suspicious+activity" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Unfreeze Account

```bash
curl -X POST http://localhost:8080/savings/accounts/1/unfreeze \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Close Account

```bash
curl -X POST http://localhost:8080/savings/accounts/1/close \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Calculate Interest

```bash
curl -X POST "http://localhost:8080/savings/accounts/calculate-interest?asOfDate=2024-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Account Statistics

```bash
curl -X GET "http://localhost:8080/savings/accounts/1/statistics?startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Portfolio Summary

```bash
curl -X GET http://localhost:8080/savings/portfolio/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Savings Statistics

```bash
curl -X GET http://localhost:8080/savings/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Users

**Base Path**: `/api/admin/users`

### Get All Users

```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create User

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"user@example.com","password":"password","role":"MEMBER"}'
```

### Update User Role

```bash
curl -X PUT http://localhost:8080/api/admin/users/1/role \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"OFFICER"}'
```

### Suspend User

```bash
curl -X PUT http://localhost:8080/api/admin/users/1/suspend \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"suspended":true,"reason":"Policy violation"}'
```

### Delete User

```bash
curl -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get User by ID

```bash
curl -X GET http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Quick Testing Guide

### 1. Login First

```bash
# Save the response to get the token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password","rememberMe":false}' \
  | jq -r '.token'
```

### 2. Set Token as Environment Variable

```bash
export TOKEN="your_token_here"
```

### 3. Use Token in Requests

```bash
curl -X GET http://localhost:8080/members \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Pretty Print JSON Responses

```bash
# Install jq if not already installed: brew install jq
curl -X GET http://localhost:8080/members \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## Common HTTP Status Codes

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

**Last Updated**: 2025-12-09
