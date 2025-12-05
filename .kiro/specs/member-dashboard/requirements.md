# Requirements Document

## Introduction

The Member Dashboard is a transparency-focused interface designed for Village Savings and Loan Association (VSLA) members to access their personal financial information via mobile devices. The primary goal is to address trust issues between members and committees by providing real-time visibility into individual funds, transactions, and obligations. This dashboard empowers members to verify their financial standing, plan loan requests, and monitor guarantor responsibilities.

## Glossary

- **Member Dashboard**: The user interface providing VSLA members access to their personal financial data
- **VSLA**: Village Savings and Loan Association
- **Digital Passbook**: Electronic representation of a member's savings record
- **Share**: Unit of savings contribution held by a member
- **Welfare Fund**: Collective fund providing benefits such as funeral grants to eligible members
- **Guarantor**: A member who provides assurance for another member's loan repayment
- **Transaction Timeline**: Chronological record of all financial interactions
- **Loan Simulator**: Tool for calculating estimated loan repayment schedules
- **Officer**: Committee member authorized to process transactions
- **Principal**: Original loan amount borrowed, excluding interest
- **Accrued Interest**: Interest accumulated on a loan over time

## Requirements

### Requirement 1

**User Story:** As a VSLA member, I want to view my digital passbook summary, so that I can quickly understand my current financial standing with the association.

#### Acceptance Criteria

1. WHEN a member accesses the dashboard THEN the Member Dashboard SHALL display a card-based layout featuring the member's name and photo
2. WHEN the passbook summary is displayed THEN the Member Dashboard SHALL show the total savings value as the current value of shares held
3. WHEN the passbook summary is displayed THEN the Member Dashboard SHALL show the outstanding loan amount as principal plus accrued interest remaining
4. WHEN the passbook summary is displayed THEN the Member Dashboard SHALL show the welfare fund status indicating eligibility for benefits
5. WHEN a member toggles the show/hide control THEN the Member Dashboard SHALL obscure or reveal sensitive balance information

### Requirement 2

**User Story:** As a VSLA member, I want to see a chronological timeline of all my transactions, so that I can verify that my deposits and withdrawals were properly recorded.

#### Acceptance Criteria

1. WHEN a member views the transaction timeline THEN the Member Dashboard SHALL display all transactions in reverse chronological order
2. WHEN displaying each transaction THEN the Member Dashboard SHALL show the transaction date, type, amount, and processing officer name
3. WHEN a member deposits funds at a meeting THEN the Member Dashboard SHALL reflect the new transaction within the timeline immediately after recording
4. WHEN a member searches for a specific transaction THEN the Member Dashboard SHALL filter the timeline based on date range or transaction type

### Requirement 3

**User Story:** As a VSLA member, I want to simulate loan scenarios, so that I can plan my finances before requesting a loan and understand the repayment obligations.

#### Acceptance Criteria

1. WHEN a member accesses the loan simulator THEN the Member Dashboard SHALL provide input fields for desired loan amount and repayment period in months
2. WHEN a member enters loan parameters THEN the Member Dashboard SHALL calculate and display the estimated monthly installment
3. WHEN a member enters loan parameters THEN the Member Dashboard SHALL calculate and display the total interest payable
4. WHEN calculating loan estimates THEN the Member Dashboard SHALL use the interest rate variables defined in the loan type configuration
5. WHEN a member enters an invalid loan amount or period THEN the Member Dashboard SHALL display validation errors and prevent calculation

### Requirement 4

**User Story:** As a VSLA member who guarantees loans for others, I want to track my guarantor obligations, so that I can monitor the repayment status and manage my liability risk.

#### Acceptance Criteria

1. WHEN a member has active guarantor obligations THEN the Member Dashboard SHALL display all loans the member is guaranteeing
2. WHEN displaying each guaranteed loan THEN the Member Dashboard SHALL show the borrower name, loan amount, and current repayment status
3. WHEN a guaranteed loan becomes overdue THEN the Member Dashboard SHALL display an alert notification to the guarantor
4. WHEN a borrower misses a payment THEN the Member Dashboard SHALL notify the guarantor immediately
5. WHEN a guaranteed loan is fully repaid THEN the Member Dashboard SHALL remove the obligation from the active guarantor list
