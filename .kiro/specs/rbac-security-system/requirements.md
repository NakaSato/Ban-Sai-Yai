# Requirements Document

## Introduction

This specification defines a comprehensive Role-Based Access Control (RBAC) security system for the Ban Sai Yai Savings Group Financial Accounting System. The system implements a hierarchical RBAC model with Separation of Duties (SoD) enforcement, relationship-based access control for guarantors, and comprehensive audit logging. The implementation will ensure that each user is assigned exactly one primary role with clearly defined permissions, preventing unauthorized access and maintaining data integrity through segregation of duties.

## Glossary

- **RBAC (Role-Based Access Control)**: A security model that restricts system access based on user roles
- **SoD (Separation of Duties)**: A security principle requiring that critical tasks be divided among multiple users to prevent fraud
- **System**: The Ban Sai Yai Savings Group Financial Accounting System
- **Officer (Treasurer)**: A user with ROLE_OFFICER permission, responsible for cash handling and transaction entry
- **Secretary (Accountant)**: A user with ROLE_SECRETARY permission, responsible for accounting accuracy and financial statements
- **President (Admin)**: A user with ROLE_PRESIDENT permission, responsible for approvals and executive oversight
- **Member**: A user with ROLE_MEMBER permission, restricted to viewing personal financial data
- **Permission**: A granular capability to perform a specific action (e.g., loan.approve, cash.entry)
- **Guarantor**: A member who provides collateral for another member's loan
- **Relationship-Based Access Control**: Access permissions granted based on relationships between entities
- **Audit Log**: A chronological record of system activities for security and compliance
- **Cash Zero Verification**: A reconciliation process ensuring physical cash matches digital records
- **Variance**: The difference between physical cash count and database balance

## Requirements

### Requirement 1

**User Story:** As a system administrator, I want each user to be assigned exactly one primary role, so that accountability is clear and permissions are unambiguous.

#### Acceptance Criteria

1. WHEN a new user is created THEN the System SHALL require assignment of exactly one role from the set (Officer, Secretary, President, Member)
2. WHEN a user attempts to log in THEN the System SHALL load the user's assigned role and associated permissions
3. WHEN a user's role is changed THEN the System SHALL revoke all previous role permissions and grant new role permissions
4. WHEN the System evaluates access to a resource THEN the System SHALL use only the user's current primary role
5. WHEN a user has no assigned role THEN the System SHALL deny all access except to the login page

### Requirement 2

**User Story:** As a system architect, I want permissions to be granular and mapped to roles through a matrix, so that access control is flexible and maintainable.

#### Acceptance Criteria

1. WHEN the System initializes THEN the System SHALL load the role-permission matrix from the database
2. WHEN a user attempts an action THEN the System SHALL verify the user's role has the required permission for that action
3. WHEN a permission is added to a role THEN the System SHALL immediately grant that capability to all users with that role
4. WHEN a permission is removed from a role THEN the System SHALL immediately revoke that capability from all users with that role
5. WHEN the System checks permissions THEN the System SHALL use the permission slug format (e.g., "loan.approve", "cash.entry")

### Requirement 3

**User Story:** As an Officer, I want to create and view deposits and withdrawals, so that I can process member transactions during meetings.

#### Acceptance Criteria

1. WHEN an Officer accesses the transactions module THEN the System SHALL display options to create deposits and withdrawals
2. WHEN an Officer creates a deposit or withdrawal THEN the System SHALL validate the transaction and save it to the database
3. WHEN an Officer attempts to void a transaction less than 24 hours old THEN the System SHALL allow the void operation
4. WHEN an Officer attempts to void a transaction more than 24 hours old THEN the System SHALL deny the operation
5. WHEN an Officer attempts to edit the General Ledger THEN the System SHALL deny access

### Requirement 4

**User Story:** As a Secretary, I want read-only access to cash box entries and full access to accounting codes, so that I can ensure accurate reconciliation without creating conflicts of interest.

#### Acceptance Criteria

1. WHEN a Secretary accesses the General Ledger THEN the System SHALL display all ledger entries with full read access
2. WHEN a Secretary attempts to edit the Chart of Accounts THEN the System SHALL allow the update operation
3. WHEN a Secretary attempts to create a cash deposit or withdrawal THEN the System SHALL deny the operation
4. WHEN a Secretary attempts to post a journal entry adjustment THEN the System SHALL allow the creation
5. WHEN a Secretary attempts to close an accounting period THEN the System SHALL allow the approval operation

### Requirement 5

**User Story:** As a President, I want to approve loans and set policy parameters, so that I can exercise executive oversight without operational data entry.

#### Acceptance Criteria

1. WHEN a President accesses the loans module THEN the System SHALL display all pending loan applications
2. WHEN a President approves or rejects a loan application THEN the System SHALL update the loan status and record the approval
3. WHEN a President attempts to set the dividend rate THEN the System SHALL allow the update operation
4. WHEN a President attempts to create a deposit or withdrawal THEN the System SHALL deny the operation to prevent ghost transactions
5. WHEN a President accesses audit logs THEN the System SHALL display all system activities with full read access

### Requirement 6

**User Story:** As a Member, I want to view my own financial data and public group metrics, so that I can track my savings and loans without accessing other members' information.

#### Acceptance Criteria

1. WHEN a Member accesses their profile THEN the System SHALL display only that member's personal financial data
2. WHEN a Member attempts to view another member's profile THEN the System SHALL deny access
3. WHEN a Member views the dashboard THEN the System SHALL display public group metrics without sensitive details
4. WHEN a Member attempts to create, update, or delete any transaction THEN the System SHALL deny the operation
5. WHEN a Member attempts to access administrative functions THEN the System SHALL deny access

### Requirement 7

**User Story:** As a guarantor, I want to view the repayment status of loans I have guaranteed, so that I can monitor my liability and apply peer pressure if needed.

#### Acceptance Criteria

1. WHEN a Member guarantees a loan for another member THEN the System SHALL grant the guarantor read access to that specific loan's details
2. WHEN a guarantor accesses their dashboard THEN the System SHALL display a "Guaranteed Loans" widget showing all loans they have guaranteed
3. WHEN a guarantor views a guaranteed loan THEN the System SHALL display the borrower's repayment status with a visual indicator (Green for current, Red for delinquent)
4. WHEN a loan is fully repaid or written off THEN the System SHALL remove the guarantor's access to that loan
5. WHEN a Member is not a guarantor for a loan THEN the System SHALL deny access to that loan's details

### Requirement 8

**User Story:** As an Officer, I want to perform end-of-day cash reconciliation with variance detection, so that I can ensure physical cash matches digital records.

#### Acceptance Criteria

1. WHEN an Officer clicks "Count Cash" THEN the System SHALL prompt for physical cash count entry
2. WHEN an Officer enters the physical cash count THEN the System SHALL compare it against the database balance
3. WHEN the physical count matches the database balance THEN the System SHALL allow the Officer to close the day
4. WHEN the physical count does not match the database balance THEN the System SHALL prevent the Officer from closing the day
5. WHEN a variance exists THEN the System SHALL escalate to the Secretary for discrepancy approval

### Requirement 9

**User Story:** As a Secretary, I want to approve cash discrepancies, so that the Officer can close the day after proper reconciliation.

#### Acceptance Criteria

1. WHEN a cash variance exists THEN the System SHALL notify the Secretary of the pending discrepancy
2. WHEN a Secretary reviews a cash discrepancy THEN the System SHALL display the variance amount and Officer's notes
3. WHEN a Secretary approves a discrepancy THEN the System SHALL book the variance as an expense or accounts payable
4. WHEN a Secretary approves a discrepancy THEN the System SHALL allow the day to be closed
5. WHEN a Secretary rejects a discrepancy THEN the System SHALL require the Officer to recount or correct entries

### Requirement 10

**User Story:** As a President, I want to view a watchdog dashboard with critical action logs, so that I can monitor staff integrity and detect potential fraud.

#### Acceptance Criteria

1. WHEN a President accesses the audit dashboard THEN the System SHALL display the last 10 actions involving DELETE or OVERRIDE permissions
2. WHEN a President views the staff activity heatmap THEN the System SHALL display entry volume by user with timestamps
3. WHEN a user enters data outside normal meeting hours THEN the System SHALL flag the activity as a security alert
4. WHEN a user attempts to access a page without authorization THEN the System SHALL log the attempt as a role violation
5. WHEN a President views role violation attempts THEN the System SHALL display all HTTP 403 errors with user, timestamp, and attempted resource

### Requirement 11

**User Story:** As a system administrator, I want all security-relevant actions to be logged in an audit trail, so that we can investigate incidents and maintain compliance.

#### Acceptance Criteria

1. WHEN a user performs a CREATE, UPDATE, or DELETE operation THEN the System SHALL log the action with user ID, timestamp, IP address, and affected data
2. WHEN a user approves or rejects a loan THEN the System SHALL log the approval action with old and new values
3. WHEN a user's role is changed THEN the System SHALL log the role change with the administrator who made the change
4. WHEN a user attempts an unauthorized action THEN the System SHALL log the attempt with the denied permission
5. WHEN an audit log entry is created THEN the System SHALL store old values and new values in JSON format for rollback capability

### Requirement 12

**User Story:** As a developer, I want the RBAC system to use a normalized database schema, so that roles and permissions can be managed efficiently.

#### Acceptance Criteria

1. WHEN the System initializes THEN the System SHALL create tables for roles, permissions, role_permissions, users, and system_audit_log
2. WHEN a role is assigned to a user THEN the System SHALL store the role_id foreign key in the users table
3. WHEN permissions are queried for a user THEN the System SHALL join users, roles, role_permissions, and permissions tables
4. WHEN a permission is added or removed THEN the System SHALL update only the role_permissions junction table
5. WHEN an audit log entry is created THEN the System SHALL store the entry with a foreign key to the users table

### Requirement 13

**User Story:** As a system administrator, I want to manage users and assign roles through an administrative interface, so that I can control system access.

#### Acceptance Criteria

1. WHEN a President accesses the user management page THEN the System SHALL display all users with their assigned roles and status
2. WHEN a President creates a new user THEN the System SHALL require username, password, member linkage, and role assignment
3. WHEN a President updates a user's role THEN the System SHALL validate the new role and update the user record
4. WHEN a President suspends a user THEN the System SHALL set the user status to "Suspended" and deny login
5. WHEN a President deletes a user THEN the System SHALL soft-delete the user record and preserve audit logs

### Requirement 14

**User Story:** As a security-conscious developer, I want the system to enforce password security and prevent brute-force attacks, so that user accounts remain secure.

#### Acceptance Criteria

1. WHEN a user creates a password THEN the System SHALL require a minimum of 8 characters with at least one uppercase, one lowercase, and one number
2. WHEN a user's password is stored THEN the System SHALL hash the password using bcrypt with a cost factor of 12
3. WHEN a user fails to log in 5 times within 15 minutes THEN the System SHALL lock the account for 30 minutes
4. WHEN a locked account attempts to log in THEN the System SHALL display a message indicating the lockout duration
5. WHEN a user successfully logs in after failed attempts THEN the System SHALL reset the failed login counter

### Requirement 15

**User Story:** As an Officer, I want the system to prevent me from approving my own transactions, so that segregation of duties is enforced.

#### Acceptance Criteria

1. WHEN an Officer creates a transaction THEN the System SHALL record the Officer's user ID as the creator
2. WHEN a transaction requires approval THEN the System SHALL display it in the approval queue
3. WHEN an Officer attempts to approve a transaction they created THEN the System SHALL deny the approval
4. WHEN a different Officer or higher-level user approves the transaction THEN the System SHALL allow the approval
5. WHEN a transaction is approved THEN the System SHALL record the approver's user ID separately from the creator's ID
