# Requirements Document

## Introduction

The President Dashboard is an Executive Information System (EIS) designed for the President role in a savings group management system. Unlike operational dashboards focused on transaction entry, this dashboard provides strategic oversight through trend analysis, risk management, and policy decision support. The dashboard consists of four specialized widgets that enable the President to monitor portfolio health, track organizational growth, manage loan approvals, and project member dividends.

## Glossary

- **President**: The executive role responsible for strategic decisions, risk management, and governance of the savings group
- **Portfolio At Risk (PAR)**: A measure of loan portfolio health based on the number of days loans are overdue
- **Savings Group**: A member-owned financial cooperative where members pool savings and provide loans to each other
- **Guarantor**: A member who pledges to repay a loan if the borrower defaults
- **Dividend**: The distribution of profits to members based on their share ownership
- **Debt-to-Savings Ratio**: The ratio of a member's outstanding loan balance to their total savings
- **Executive Information System (EIS)**: A dashboard providing high-level strategic information for executive decision-making
- **Dashboard System**: The web application that displays the President Dashboard widgets
- **Loan Application**: A request by a member to borrow funds from the savings group
- **Statutory Reserves**: Legally required funds set aside from profits before dividend distribution
- **Total Assets**: The sum of all savings deposits and retained earnings in the savings group

## Requirements

### Requirement 1

**User Story:** As a President, I want to monitor portfolio risk through a visual PAR radar, so that I can identify loan repayment issues and take corrective action before defaults escalate.

#### Acceptance Criteria

1. WHEN the President views the PAR radar widget THEN the Dashboard System SHALL display loan portfolio segmented by overdue status using a donut chart or traffic light visualization
2. WHEN calculating PAR segments THEN the Dashboard System SHALL classify loans as Current (0 days overdue), Watchlist (1-30 days overdue), Substandard (31-90 days overdue), or Default (>90 days overdue)
3. WHEN the President clicks on a PAR segment THEN the Dashboard System SHALL display a detailed list of members in that category with their loan amounts and guarantor information
4. WHEN displaying defaulting members THEN the Dashboard System SHALL include guarantor details from the member reference table to enable recovery proceedings
5. WHEN PAR data is requested THEN the Dashboard System SHALL calculate overdue days by comparing loan due dates against the current date or payment timestamps

### Requirement 2

**User Story:** As a President, I want to track capital growth and membership trends over time, so that I can assess the health and stability of the savings group.

#### Acceptance Criteria

1. WHEN the President views the capital growth widget THEN the Dashboard System SHALL display total assets calculated as the sum of total savings and retained earnings
2. WHEN the President views the membership trends widget THEN the Dashboard System SHALL display total membership count over a 12-month rolling period
3. WHEN rendering the trends visualization THEN the Dashboard System SHALL use a dual-axis line chart showing both capital growth and membership count
4. WHEN trend data shows declining values THEN the Dashboard System SHALL provide visual indicators that enable the President to identify potential trust or economic issues
5. WHEN calculating total assets THEN the Dashboard System SHALL query current savings balances and retained earnings from the financial records

### Requirement 3

**User Story:** As a President, I want to review and approve pending loan applications, so that I can ensure responsible lending and protect the savings group from excessive risk.

#### Acceptance Criteria

1. WHEN the President views the loan approval queue THEN the Dashboard System SHALL display all pending loan applications with applicant name, loan amount, purpose, debt-to-savings ratio, and guarantor status
2. WHEN displaying loan applications THEN the Dashboard System SHALL categorize loan purpose as Emergency or Production
3. WHEN a loan application presents high risk THEN the Dashboard System SHALL flag applications where the applicant has high existing debt or the guarantor is over-leveraged with a warning indicator
4. WHEN the President selects approve on a loan application THEN the Dashboard System SHALL update the loan status to approved and notify relevant parties
5. WHEN the President selects reject on a loan application THEN the Dashboard System SHALL update the loan status to rejected and record the decision

### Requirement 4

**User Story:** As a President, I want to view projected dividend rates, so that I can communicate financial expectations to members and incentivize continued savings participation.

#### Acceptance Criteria

1. WHEN the President views the dividend projection widget THEN the Dashboard System SHALL display the estimated dividend per share
2. WHEN calculating dividend per share THEN the Dashboard System SHALL compute total income minus total expenses minus statutory reserves divided by total shares outstanding
3. WHEN dividend data is unavailable THEN the Dashboard System SHALL display a message indicating insufficient data for projection
4. WHEN displaying dividend projections THEN the Dashboard System SHALL source data from dividend and dividend plan tables
5. WHEN the dividend projection updates THEN the Dashboard System SHALL enable the President to communicate expected returns to members for planning purposes
