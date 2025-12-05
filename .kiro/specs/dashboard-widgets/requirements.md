# Requirements Document

## Introduction

The Ban Sai Yai Savings Group dashboard system requires specific, role-based widgets that provide financial officers, secretaries, presidents, and members with the tools and information they need to manage daily operations, maintain accounting accuracy, monitor strategic health, and build member trust. This feature encompasses the implementation of specialized dashboard widgets with embedded business logic, real-time calculations, and interactive modals for transaction processing.

## Glossary

- **Widget**: A modular dashboard component displaying specific financial information or providing transaction functionality
- **Teller Action Card**: The primary Officer interface for recording member deposits and loan payments
- **Cash Box Tally**: Real-time cash control widget showing total in, total out, and net cash with reconciliation features
- **Transaction Feed**: Live display of recent transactions with receipt generation capability
- **Trial Balance Widget**: Secretary tool displaying the double-entry accounting balance state
- **Unclassified Transaction Alert**: Warning widget showing transactions not yet mapped to accounting codes
- **Financial Statement Previews**: Quick-view charts of income statement and balance sheet data
- **PAR Analysis (Portfolio at Risk)**: Risk monitoring widget categorizing loans by delinquency status
- **Liquidity Gauge**: Strategic indicator showing the ratio of liquid assets to savings deposits
- **Membership Growth Trend**: Chart tracking new member registrations versus resignations
- **Digital Passbook**: Member-facing widget displaying savings balance and transaction history
- **Loan Obligation Card**: Member reminder showing outstanding principal and next payment due
- **Dividend Estimator**: Calculator showing projected year-end dividend based on member shares
- **Fiscal Period Indicator**: Header element showing whether the current accounting period is open or closed
- **Omni-Search Bar**: Global search interface for rapid member lookup by name or ID
- **Modal**: Popup dialog for data entry (e.g., deposit amount, loan payment details)
- **Auto-complete**: Dynamic dropdown that suggests results as the user types
- **AJAX**: Asynchronous data loading technique for fetching member details without page refresh
- **Receipt Generation**: PDF creation functionality triggered by clicking receipt icons

## Requirements

### Requirement 1

**User Story:** As an Officer, I want a centralized Teller Action Card that allows me to record deposits and loan payments for any member, so that I can process transactions quickly during busy meeting times without navigating multiple pages.

#### Acceptance Criteria

1. WHEN an Officer selects a member from the auto-complete field THEN the Teller Action Card SHALL fetch and display the member's current savings balance, outstanding loan principal, and loan status via AJAX
2. WHEN an Officer clicks the Deposit button THEN the Teller Action Card SHALL open a Modal with an amount input field
3. WHEN an Officer clicks the Loan Pay button THEN the Teller Action Card SHALL open a Modal with fields for principal amount, interest amount, and fine amount
4. WHEN an Officer submits a loan payment THEN the Teller Action Card SHALL auto-calculate the minimum interest due based on the loan type interest rate
5. WHEN a transaction is successfully recorded THEN the Teller Action Card SHALL clear the form and display a success confirmation

### Requirement 2

**User Story:** As an Officer, I want a Cash Box Tally widget that shows real-time totals of cash in and cash out, so that I can monitor cash flow and reconcile the physical cash at the end of the meeting.

#### Acceptance Criteria

1. WHEN the Officer dashboard loads THEN the Cash Box Tally SHALL calculate and display the sum of all deposits, repayments, and fees for the current date as "Total In"
2. WHEN the Officer dashboard loads THEN the Cash Box Tally SHALL calculate and display the sum of all withdrawals and loan disbursements for the current date as "Total Out"
3. WHEN the Cash Box Tally displays totals THEN the Cash Box Tally SHALL show a "Net Cash" value calculated as Total In minus Total Out
4. WHEN an Officer clicks "Count Cash" THEN the Cash Box Tally SHALL reveal a denomination entry section for physical cash counting
5. WHEN an Officer enters denomination counts THEN the Cash Box Tally SHALL calculate the physical total and display the variance against the database value

### Requirement 3

**User Story:** As an Officer, I want a Transaction Feed widget that displays the most recent transactions, so that I can visually confirm that my data entries were recorded correctly.

#### Acceptance Criteria

1. WHEN the Officer dashboard loads THEN the Transaction Feed SHALL display the 10 most recent transactions ordered by timestamp descending
2. WHEN displaying transactions THEN the Transaction Feed SHALL show the time, member name, transaction type, and amount for each entry
3. WHEN displaying transactions THEN the Transaction Feed SHALL include a receipt icon for each transaction
4. WHEN an Officer clicks a receipt icon THEN the Transaction Feed SHALL open a new window with a printable PDF receipt for that transaction
5. WHEN a new transaction is recorded THEN the Transaction Feed SHALL refresh to include the new entry at the top of the list

### Requirement 4

**User Story:** As a Secretary, I want a Trial Balance Widget that immediately shows whether the accounting ledger is balanced, so that I can detect double-entry errors before generating monthly reports.

#### Acceptance Criteria

1. WHEN the Secretary dashboard loads THEN the Trial Balance Widget SHALL calculate the sum of all debit entries for the active fiscal period
2. WHEN the Secretary dashboard loads THEN the Trial Balance Widget SHALL calculate the sum of all credit entries for the active fiscal period
3. WHEN the debits equal the credits THEN the Trial Balance Widget SHALL display a single green progress bar indicating a balanced state
4. WHEN the debits do not equal the credits THEN the Trial Balance Widget SHALL display a split progress bar with red and blue segments showing the magnitude of the imbalance
5. WHEN the ledger is unbalanced THEN the Trial Balance Widget SHALL prevent the Secretary from generating the monthly report

### Requirement 5

**User Story:** As a Secretary, I want an Unclassified Transaction Alert widget that shows how many transactions lack accounting codes, so that I can quickly identify and classify them during reconciliation.

#### Acceptance Criteria

1. WHEN the Secretary dashboard loads THEN the Unclassified Transaction Alert SHALL count all transactions where the accounting code field is null
2. WHEN unclassified transactions exist THEN the Unclassified Transaction Alert SHALL display a warning card showing the count
3. WHEN the Secretary clicks the warning card THEN the Unclassified Transaction Alert SHALL navigate to the Journal Entry screen
4. WHEN navigating to the Journal Entry screen THEN the Unclassified Transaction Alert SHALL pre-filter the view to show only unclassified transactions
5. WHEN all transactions are classified THEN the Unclassified Transaction Alert SHALL display a success message indicating no action is needed

### Requirement 6

**User Story:** As a Secretary, I want Financial Statement Preview widgets that show quick visualizations of income and balance sheet data, so that I can assess the group's financial health at a glance.

#### Acceptance Criteria

1. WHEN the Secretary dashboard loads THEN the Financial Statement Previews SHALL display a bar chart comparing interest income plus fees versus expenses
2. WHEN the Secretary dashboard loads THEN the Financial Statement Previews SHALL display a pie chart showing asset distribution across loans, cash, and bank balances
3. WHEN generating chart data THEN the Financial Statement Previews SHALL aggregate data from accounting entries grouped by account type codes
4. WHEN displaying income data THEN the Financial Statement Previews SHALL include all entries from account code group 4xxx
5. WHEN displaying asset data THEN the Financial Statement Previews SHALL include all entries from account code group 1xxx

### Requirement 7

**User Story:** As a President, I want a PAR Analysis widget that categorizes loans by delinquency status, so that I can monitor portfolio risk and identify members requiring follow-up.

#### Acceptance Criteria

1. WHEN the President dashboard loads THEN the PAR Analysis SHALL iterate through all active loans and compare the last payment date with the current date
2. WHEN categorizing loans THEN the PAR Analysis SHALL classify loans as Standard when 0-30 days since last payment
3. WHEN categorizing loans THEN the PAR Analysis SHALL classify loans as Watch when 31-60 days since last payment
4. WHEN categorizing loans THEN the PAR Analysis SHALL classify loans as Substandard when 61-90 days since last payment
5. WHEN categorizing loans THEN the PAR Analysis SHALL classify loans as Loss when more than 90 days since last payment
6. WHEN displaying PAR data THEN the PAR Analysis SHALL render a doughnut chart with segments for each category
7. WHEN a President clicks a chart segment THEN the PAR Analysis SHALL open a modal table listing the specific members and their guarantors in that category

### Requirement 8

**User Story:** As a President, I want a Liquidity Gauge widget that shows the ratio of liquid assets to savings deposits, so that I can ensure the group has sufficient cash to meet member withdrawal requests.

#### Acceptance Criteria

1. WHEN the President dashboard loads THEN the Liquidity Gauge SHALL calculate the sum of cash and bank balances
2. WHEN the President dashboard loads THEN the Liquidity Gauge SHALL calculate the total of all member savings deposits
3. WHEN displaying liquidity THEN the Liquidity Gauge SHALL calculate the ratio as liquid assets divided by total savings deposits expressed as a percentage
4. WHEN the ratio is less than 5 percent THEN the Liquidity Gauge SHALL display a red zone indicator with "Crisis" status
5. WHEN the ratio is between 5 and 10 percent THEN the Liquidity Gauge SHALL display a yellow zone indicator with "Caution" status
6. WHEN the ratio is between 10 and 20 percent THEN the Liquidity Gauge SHALL display a green zone indicator with "Healthy" status
7. WHEN the ratio is greater than 20 percent THEN the Liquidity Gauge SHALL display a blue zone indicator with "Inefficient" status

### Requirement 9

**User Story:** As a President, I want a Membership Growth Trend widget that tracks new registrations versus resignations, so that I can monitor the social reach and sustainability of the group.

#### Acceptance Criteria

1. WHEN the President dashboard loads THEN the Membership Growth Trend SHALL query the member table for registration dates
2. WHEN displaying growth data THEN the Membership Growth Trend SHALL aggregate new member counts by month
3. WHEN displaying growth data THEN the Membership Growth Trend SHALL aggregate resignation counts by month
4. WHEN rendering the chart THEN the Membership Growth Trend SHALL display a line chart with separate lines for new members and resignations
5. WHEN resignations spike THEN the Membership Growth Trend SHALL visually highlight the red resignation line to draw attention

### Requirement 10

**User Story:** As a Member, I want a Digital Passbook widget that displays my current savings balance, so that I can verify my contributions and build trust in the system.

#### Acceptance Criteria

1. WHEN a Member views their dashboard THEN the Digital Passbook SHALL display the member's last month forward balance
2. WHEN a Member views their dashboard THEN the Digital Passbook SHALL add all deposit transactions from the current month to the forward balance
3. WHEN displaying the balance THEN the Digital Passbook SHALL show the total in large, prominent font
4. WHEN displaying the balance THEN the Digital Passbook SHALL include a "Last activity" timestamp showing the most recent transaction date
5. WHEN the member has no transactions THEN the Digital Passbook SHALL display the forward balance with a message indicating no recent activity

### Requirement 11

**User Story:** As a Member, I want a Loan Obligation Card that shows my outstanding debt and next payment due date, so that I can plan my finances and avoid late payments.

#### Acceptance Criteria

1. WHEN a Member with an active loan views their dashboard THEN the Loan Obligation Card SHALL display the current outstanding principal amount
2. WHEN a Member with an active loan views their dashboard THEN the Loan Obligation Card SHALL display the next payment due date
3. WHEN a Member with an active loan views their dashboard THEN the Loan Obligation Card SHALL calculate and display the estimated interest due
4. WHEN the next payment due date has passed THEN the Loan Obligation Card SHALL change the card border to red to indicate overdue status
5. WHEN a Member has no active loans THEN the Loan Obligation Card SHALL display a message indicating no current loan obligations

### Requirement 12

**User Story:** As a Member, I want a Dividend Estimator widget that projects my year-end dividend, so that I am incentivized to increase my savings contributions.

#### Acceptance Criteria

1. WHEN a Member views their dashboard THEN the Dividend Estimator SHALL fetch the member's total share capital
2. WHEN a Member views their dashboard THEN the Dividend Estimator SHALL fetch the current projected dividend rate from system configuration
3. WHEN calculating the estimate THEN the Dividend Estimator SHALL multiply the member's shares by the projected rate
4. WHEN displaying the estimate THEN the Dividend Estimator SHALL show the calculated dividend amount with a disclaimer
5. WHEN displaying the estimate THEN the Dividend Estimator SHALL include the text "Estimate only Subject to final committee approval"

### Requirement 13

**User Story:** As any user, I want a Fiscal Period Indicator in the dashboard header that shows whether the current accounting period is open or closed, so that I understand whether transactions can be recorded.

#### Acceptance Criteria

1. WHEN any dashboard loads THEN the Fiscal Period Indicator SHALL query the system configuration table for the current period status
2. WHEN the period is open THEN the Fiscal Period Indicator SHALL display a green badge with text "Period [Month Year] OPEN"
3. WHEN the period is closed THEN the Fiscal Period Indicator SHALL display a red badge with text "Period [Month Year] CLOSED"
4. WHEN the period is closed THEN the Fiscal Period Indicator SHALL disable all "Add Transaction" buttons on the Officer dashboard via JavaScript
5. WHEN the period status changes THEN the Fiscal Period Indicator SHALL update the display without requiring a page refresh

### Requirement 14

**User Story:** As any user, I want an Omni-Search Bar in the dashboard header that allows me to quickly find members by name or ID, so that I can access member profiles without navigating through lists.

#### Acceptance Criteria

1. WHEN a user types in the Omni-Search Bar THEN the Omni-Search Bar SHALL query the member table for matches on member ID, first name, last name, or national ID
2. WHEN displaying search results THEN the Omni-Search Bar SHALL limit results to 5 members
3. WHEN displaying search results THEN the Omni-Search Bar SHALL show each member's ID, full name, and status in a dropdown list
4. WHEN a user clicks a search result THEN the Omni-Search Bar SHALL navigate to the member profile page for that member
5. WHEN no matches are found THEN the Omni-Search Bar SHALL display a message indicating no results

### Requirement 15

**User Story:** As a system administrator, I want all dashboard widgets to implement proper error handling, so that widget failures do not crash the entire dashboard or expose sensitive error details to users.

#### Acceptance Criteria

1. WHEN a widget data fetch fails THEN the Dashboard System SHALL display a user-friendly error message in that widget only
2. WHEN a widget encounters an error THEN the Dashboard System SHALL log the detailed error to the server console for debugging
3. WHEN a widget fails to load THEN the Dashboard System SHALL continue rendering all other widgets successfully
4. WHEN a widget displays an error THEN the Dashboard System SHALL provide a "Retry" button to attempt reloading the widget data
5. WHEN a widget error is transient THEN the Dashboard System SHALL allow the user to recover without refreshing the entire page
