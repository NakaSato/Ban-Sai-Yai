# Requirements Document

## Introduction

This document specifies the requirements for the Secretary Dashboard, an "Accounting Cockpit" designed to ensure financial integrity, General Ledger (GL) balance, and compliance for the Ban Sai Yai Savings Group system. The Secretary's role transitions from operational entry to financial control, requiring specialized widgets for trial balance monitoring, revenue/expense analysis, liquidity management, and report generation. The dashboard enforces accounting controls by preventing month-end closing when imbalances exist and provides real-time visibility into the group's profitability and cash position.

## Glossary

- **System**: The Ban Sai Yai Savings Group Financial Accounting System
- **Secretary**: A user with ROLE_SECRETARY permission responsible for accounting reconciliation and financial reporting
- **Trial Balance**: An accounting report where the sum of all debit entries must equal the sum of all credit entries
- **General Ledger (GL)**: The complete record of financial transactions organized by account codes
- **Fiscal Period**: A monthly accounting period that can be in "Open" or "Closed" state
- **Transaction Table**: The database table storing all financial transactions with debit/credit indicators
- **Accounting Table**: The database table storing the chart of accounts with account codes and classifications
- **Account Code**: A numeric identifier for accounts following the pattern: 1xxx (Assets), 2xxx (Liabilities), 3xxx (Equity), 4xxx (Income), 5xxx (Expenses)
- **Imbalance**: A condition where total debits do not equal total credits in the trial balance
- **Revenue**: Income generated from operations, including interest on loans (4xxx), entrance fees, and fines
- **Expense**: Costs incurred in operations, including office supplies (5xxx), utilities, and committee allowances
- **Liquidity**: The availability of liquid assets (cash and bank deposits) to meet obligations
- **Cash in Hand**: Physical cash held by the organization for daily operations
- **Bank Deposits**: Funds held in commercial bank accounts (e.g., Krung Thai Bank, GSB)
- **Journal Entry**: A manual accounting entry to record transactions or adjustments
- **End of Month Closing**: The process of finalizing a fiscal period and preventing further transaction posting
- **Accrual-Based Accounting**: An accounting method where revenues and expenses are recorded when earned or incurred, not when cash is exchanged

## Requirements

### Requirement 1

**User Story:** As a Secretary, I want to monitor the trial balance status in real-time, so that I can immediately detect accounting imbalances and prevent month-end closing until the books are balanced.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard loads THEN the System SHALL calculate the sum of all debit amounts from the transaction table for the active fiscal period
2. WHEN the Secretary Dashboard loads THEN the System SHALL calculate the sum of all credit amounts from the transaction table for the active fiscal period
3. WHEN calculating trial balance THEN the System SHALL join the transaction table with the accounting table using the acc_id foreign key
4. WHEN the sum of debits equals the sum of credits THEN the System SHALL display a balanced scale icon or a single green progress bar with text "Trial Balance: BALANCED"
5. WHEN the sum of debits does not equal the sum of credits THEN the System SHALL display a flashing "Imbalance Alert" indicator with the variance amount
6. WHEN an imbalance exists THEN the System SHALL prevent the Secretary from executing the "End of Month" closing process
7. WHEN the Secretary clicks on the trial balance widget THEN the System SHALL navigate to the Journal Entry screen to investigate discrepancies
8. WHEN displaying the trial balance widget THEN the System SHALL show total debits, total credits, and the difference in a comparative bar chart format

### Requirement 2

**User Story:** As a Secretary, I want to track revenue and expense categories in real-time, so that I can monitor the group's profitability and ensure operating expenses do not consume the dividend pool.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard loads THEN the System SHALL aggregate all income transactions from the transaction table where the linked account code is in the 4xxx range
2. WHEN the Secretary Dashboard loads THEN the System SHALL aggregate all expense transactions from the transaction table where the linked account code is in the 5xxx range
3. WHEN aggregating income THEN the System SHALL include interest on loans, entrance fees, and fines as separate subcategories
4. WHEN aggregating expenses THEN the System SHALL include office supplies, utilities, and committee allowances as separate subcategories
5. WHEN displaying revenue and expense analysis THEN the System SHALL render a stacked bar chart or waterfall chart showing income categories versus expense categories
6. WHEN displaying the analysis THEN the System SHALL calculate and display net profit as total income minus total expenses
7. WHEN net profit is negative THEN the System SHALL display the widget with a warning indicator showing that expenses exceed income
8. WHEN the Secretary clicks on a category in the chart THEN the System SHALL drill down to show individual transactions within that category

### Requirement 3

**User Story:** As a Secretary, I want to manage the split between cash in hand and bank deposits, so that I can maintain optimal liquidity while ensuring cash safety and meeting withdrawal demands.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard loads THEN the System SHALL query the accounting table for the current balance of the "Cash in Hand" account (account code 1010)
2. WHEN the Secretary Dashboard loads THEN the System SHALL query the accounting table for the current balance of the "Bank Deposits" account (account code 1020)
3. WHEN displaying liquidity management THEN the System SHALL render a comparative view showing "Cash in Hand" and "Bank Deposits" as side-by-side bars or cards
4. WHEN the Cash in Hand balance exceeds 50000 THB THEN the System SHALL display an alert with text "Cash exceeds safety threshold. Consider bank deposit."
5. WHEN the Cash in Hand balance falls below 10000 THB THEN the System SHALL display an alert with text "Low cash reserves. Consider bank withdrawal."
6. WHEN the Secretary clicks the "Make Bank Deposit" action button THEN the System SHALL open a modal to create a manual journal entry transferring cash to bank
7. WHEN the Secretary clicks the "Make Bank Withdrawal" action button THEN the System SHALL open a modal to create a manual journal entry transferring bank funds to cash
8. WHEN displaying the liquidity widget THEN the System SHALL show the total liquid assets as the sum of Cash in Hand and Bank Deposits

### Requirement 4

**User Story:** As a Secretary, I want a centralized hub for generating statutory and management reports, so that I can produce required financial statements with one click for committee review and auditing.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard loads THEN the System SHALL display a Report Generation Hub widget with a grid of report shortcuts
2. WHEN the Report Generation Hub displays THEN the System SHALL include buttons for Monthly Trial Balance, Statement of Income and Expenses, and Balance Sheet
3. WHEN the Secretary clicks the "Generate Monthly Trial Balance" button THEN the System SHALL create a PDF report showing all account balances with debits and credits for the active fiscal period
4. WHEN the Secretary clicks the "Generate Statement of Income and Expenses" button THEN the System SHALL create a PDF report showing all revenue and expense accounts with totals and net profit
5. WHEN the Secretary clicks the "Generate Balance Sheet" button THEN the System SHALL create a PDF report showing assets, liabilities, and equity accounts with totals
6. WHEN a report is generated THEN the System SHALL include the fiscal period, generation date, and signature lines for committee members
7. WHEN a report is generated THEN the System SHALL trigger a browser download of the PDF file
8. WHEN the trial balance is unbalanced THEN the System SHALL disable the report generation buttons and display a message "Cannot generate reports while trial balance is unbalanced"

### Requirement 5

**User Story:** As a Secretary, I want to see a summary of key financial metrics at the top of my dashboard, so that I can quickly assess the overall financial status before diving into details.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard loads THEN the System SHALL display a summary card showing the current fiscal period month and year
2. WHEN the Secretary Dashboard loads THEN the System SHALL display a summary card showing the total number of unclassified transactions
3. WHEN the Secretary Dashboard loads THEN the System SHALL display a summary card showing the trial balance status as "Balanced" or "Imbalanced"
4. WHEN the Secretary Dashboard loads THEN the System SHALL display a summary card showing the total liquid assets (cash plus bank)
5. WHEN any summary metric indicates a problem THEN the System SHALL highlight that card with a warning or error color

### Requirement 6

**User Story:** As a Secretary, I want to access the Journal Entry screen directly from dashboard alerts, so that I can quickly resolve accounting issues without navigating through multiple pages.

#### Acceptance Criteria

1. WHEN the trial balance widget shows an imbalance THEN the System SHALL display a "View Journal Entries" button within the widget
2. WHEN the unclassified transactions alert is displayed THEN the System SHALL display a "Classify Transactions" button within the alert
3. WHEN the Secretary clicks "View Journal Entries" from the trial balance widget THEN the System SHALL navigate to the Journal Entry screen
4. WHEN the Secretary clicks "Classify Transactions" from the unclassified alert THEN the System SHALL navigate to the Journal Entry screen with a filter applied to show only unclassified transactions
5. WHEN navigating to the Journal Entry screen from a dashboard widget THEN the System SHALL preserve the context so the Secretary can return to the dashboard after making corrections

### Requirement 7

**User Story:** As a Secretary, I want the dashboard to refresh automatically when accounting data changes, so that I always see current information without manual page reloads.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard is displayed THEN the System SHALL poll the backend API every 30 seconds to check for data updates
2. WHEN new transactions are posted by Officers THEN the System SHALL automatically refresh the trial balance widget within 30 seconds
3. WHEN journal entries are created or modified THEN the System SHALL automatically refresh the revenue and expense analysis widget within 30 seconds
4. WHEN bank transactions are recorded THEN the System SHALL automatically refresh the liquidity management widget within 30 seconds
5. WHEN the backend API is unavailable THEN the System SHALL display a "Data may be stale" indicator on affected widgets

### Requirement 8

**User Story:** As a Secretary, I want to perform the End of Month closing process from the dashboard, so that I can finalize the fiscal period and prevent further transaction posting.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard displays THEN the System SHALL show an "End of Month" button in the dashboard header
2. WHEN the trial balance is balanced THEN the System SHALL enable the "End of Month" button
3. WHEN the trial balance is unbalanced THEN the System SHALL disable the "End of Month" button and display a tooltip explaining why
4. WHEN the Secretary clicks the "End of Month" button THEN the System SHALL display a confirmation dialog with text "Are you sure you want to close the fiscal period? This action cannot be undone."
5. WHEN the Secretary confirms the End of Month action THEN the System SHALL update the fiscal period status to "Closed" in the system_config table
6. WHEN the fiscal period is closed THEN the System SHALL prevent all Officers from posting new transactions
7. WHEN the fiscal period is closed THEN the System SHALL display a success message and update the fiscal period indicator in the dashboard header

### Requirement 9

**User Story:** As a Secretary, I want to view historical fiscal period data, so that I can compare current performance with previous months.

#### Acceptance Criteria

1. WHEN the Secretary Dashboard displays THEN the System SHALL include a fiscal period selector dropdown in the header
2. WHEN the Secretary selects a previous fiscal period THEN the System SHALL reload all dashboard widgets with data filtered to that period
3. WHEN viewing a closed fiscal period THEN the System SHALL display a read-only indicator showing that no modifications can be made
4. WHEN viewing a closed fiscal period THEN the System SHALL disable all action buttons for journal entries and report generation
5. WHEN the Secretary switches back to the current fiscal period THEN the System SHALL restore all interactive functionality

### Requirement 10

**User Story:** As a Secretary, I want error handling and validation on all dashboard actions, so that I can understand and correct issues when operations fail.

#### Acceptance Criteria

1. WHEN a dashboard widget fails to load data THEN the System SHALL display an error message within the widget container
2. WHEN a report generation fails THEN the System SHALL display a notification with the specific error message
3. WHEN a journal entry submission fails THEN the System SHALL display validation errors in the modal form
4. WHEN the backend API returns an error THEN the System SHALL log the error details to the browser console for debugging
5. WHEN a network error occurs THEN the System SHALL display a retry button allowing the Secretary to attempt the operation again
