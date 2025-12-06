// User and Authentication Types
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  PRESIDENT = "PRESIDENT",
  SECRETARY = "SECRETARY",
  OFFICER = "OFFICER",
  MEMBER = "MEMBER",
}

export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isInitialized: boolean;
  error: string | null;
}

export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  user: User;
  token: string;
  refreshToken?: string;
  expiresIn: number;
}

export interface SignUpRequest {
  username: string;
  password: string;
  confirmPassword: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  address?: string;
  dateOfBirth?: string;
}

export interface LogoutRequest {
  refreshToken?: string;
}

export interface RefreshTokenRequest {
  token: string;
}

export interface RefreshTokenResponse {
  token: string;
  refreshToken?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

// Member Types
export interface Member {
  id: string;
  memberId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  dateOfBirth: string;
  joinDate: string;
  status: MemberStatus;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum MemberStatus {
  ACTIVE = "ACTIVE",
  INACTIVE = "INACTIVE",
  SUSPENDED = "SUSPENDED",
}

// Loan Types
export interface Loan {
  id: string;
  loanNumber: string;
  memberId: string;
  member: Member;
  type: LoanType;
  amount: number;
  interestRate: number;
  term: number;
  status: LoanStatus;
  applicationDate: string;
  approvalDate?: string;
  disbursementDate?: string;
  maturityDate: string;
  purpose: string;
  createdAt: string;
  updatedAt: string;
}

export enum LoanType {
  PERSONAL = "PERSONAL",
  BUSINESS = "BUSINESS",
  EMERGENCY = "EMERGENCY",
  EDUCATION = "EDUCATION",
}

export enum LoanStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  DISBURSED = "DISBURSED",
  REJECTED = "REJECTED",
  COMPLETED = "COMPLETED",
  DEFAULTED = "DEFAULTED",
}

export interface LoanBalance {
  id: string;
  loanId: string;
  principalBalance: number;
  interestBalance: number;
  totalBalance: number;
  lastPaymentDate?: string;
  nextPaymentDate: string;
  daysPastDue: number;
  createdAt: string;
  updatedAt: string;
}

export interface Collateral {
  id: string;
  loanId: string;
  type: CollateralType;
  description: string;
  value: number;
  appraisalDate?: string;
  documents?: string[];
  createdAt: string;
  updatedAt: string;
}

export enum CollateralType {
  REAL_ESTATE = "REAL_ESTATE",
  VEHICLE = "VEHICLE",
  JEWELRY = "JEWELRY",
  EQUIPMENT = "EQUIPMENT",
  OTHER = "OTHER",
}

export interface Guarantor {
  id: string;
  loanId: string;
  name: string;
  relationship: string;
  phone: string;
  email?: string;
  address: string;
  idDocument: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Savings Types
export interface SavingAccount {
  id: string;
  accountNumber: string;
  memberId: string;
  member: Member;
  accountType: AccountType;
  balance: number;
  status: AccountStatus;
  openingDate: string;
  lastTransactionDate?: string;
  createdAt: string;
  updatedAt: string;
}

export enum AccountType {
  REGULAR = "REGULAR",
  SPECIAL = "SPECIAL",
  FIXED_DEPOSIT = "FIXED_DEPOSIT",
}

export enum AccountStatus {
  ACTIVE = "ACTIVE",
  INACTIVE = "INACTIVE",
  FROZEN = "FROZEN",
  CLOSED = "CLOSED",
}

export interface SavingTransaction {
  id: string;
  accountId: string;
  type: TransactionType;
  amount: number;
  description: string;
  referenceNumber?: string;
  transactionDate: string;
  balanceAfter: number;
  createdAt: string;
  updatedAt: string;
}

export enum TransactionType {
  DEPOSIT = "DEPOSIT",
  WITHDRAWAL = "WITHDRAWAL",
  INTEREST = "INTEREST",
  FEE = "FEE",
  TRANSFER_IN = "TRANSFER_IN",
  TRANSFER_OUT = "TRANSFER_OUT",
}

export interface SavingBalance {
  id: string;
  accountId: string;
  principalBalance: number;
  interestBalance: number;
  totalBalance: number;
  lastUpdated: string;
  createdAt: string;
  updatedAt: string;
}

// Payment Types
export interface Payment {
  id: string;
  paymentNumber: string;
  loanId: string;
  loan: Loan;
  memberId: string;
  member: Member;
  type: PaymentType;
  amount: number;
  principalAmount: number;
  interestAmount: number;
  penaltyAmount?: number;
  status: PaymentStatus;
  paymentDate: string;
  dueDate: string;
  receiptNumber?: string;
  paymentMethod?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum PaymentType {
  PRINCIPAL = "PRINCIPAL",
  INTEREST = "INTEREST",
  PENALTY = "PENALTY",
  FULL_PAYMENT = "FULL_PAYMENT",
}

export enum PaymentStatus {
  PENDING = "PENDING",
  PAID = "PAID",
  OVERDUE = "OVERDUE",
  CANCELLED = "CANCELLED",
}

// API Response Types
/**
 * Standardized API response wrapper matching backend ApiResponseWrapper
 */
export interface ApiResponseWrapper<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  requestId?: string;
  error?: ErrorDetails;
  pagination?: PageInfo;
}

export interface ErrorDetails {
  code: string;
  field?: string;
  details?: string;
}

export interface PageInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** @deprecated Use ApiResponseWrapper instead */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errors?: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Form Types
export interface MemberFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  dateOfBirth: string;
}

export interface LoanFormData {
  memberId: string;
  type: LoanType;
  amount: number;
  term: number;
  purpose: string;
  collateral?: CollateralFormData;
  guarantors?: GuarantorFormData[];
}

export interface CollateralFormData {
  type: CollateralType;
  description: string;
  value: number;
}

export interface GuarantorFormData {
  name: string;
  relationship: string;
  phone: string;
  email?: string;
  address: string;
  idDocument: string;
}

export interface PaymentFormData {
  loanId: string;
  amount: number;
  paymentMethod: string;
  notes?: string;
}

// Dashboard Types
export interface DashboardStats {
  totalMembers: number;
  activeLoans: number;
  totalSavings: number;
  totalLoanPortfolio: number;
  overduePayments: number;
  monthlyRepayments: number;
  monthlyRevenue: number;
  newMembersThisMonth: number;
  loanApplicationsPending: number;
}

export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string;
    borderWidth?: number;
  }[];
}

// Dashboard Widget Types
export interface FiscalPeriod {
  period: string;
  status: "OPEN" | "CLOSED";
}

export interface MemberSearchResult {
  memberId: number;
  firstName: string;
  lastName: string;
  thumbnailUrl: string;
  status: string;
}

export interface MemberFinancials {
  savingsBalance: number;
  loanPrincipal: number;
  loanStatus: string;
}

export interface DepositRequest {
  memberId: number;
  amount: number;
  notes?: string;
}

export interface LoanPaymentRequest {
  memberId: number;
  loanId: number;
  principalAmount: number;
  interestAmount: number;
  fineAmount?: number;
  notes?: string;
}

export interface TransactionResponse {
  transactionId: number | null;
  transactionNumber: string | null;
  type: string;
  amount: number;
  timestamp: string;
  status: string;
  message: string;
}

// Utility Types
export interface SelectOption {
  value: string | number;
  label: string;
}

export interface TableColumn {
  id: string;
  label: string;
  minWidth?: number;
  align?: "left" | "center" | "right";
  format?: (value: any) => string;
}

export interface FilterParams {
  page?: number;
  size?: number;
  sort?: string;
  order?: "asc" | "desc";
  search?: string;
  status?: string;
  type?: string;
  memberId?: string;
  loanId?: string;
  startDate?: string;
  endDate?: string;
}

// Additional Business Types for Thai Savings Group

/**
 * Share capital information for a member
 */
export interface ShareCapital {
  memberId: string;
  totalShares: number;
  shareValue: number;
  totalValue: number;
  lastContributionDate?: string;
}

/**
 * Dividend calculation result
 */
export interface DividendCalculation {
  memberId: string;
  memberName: string;
  shareCapital: number;
  interestIncome: number;
  dividendRate: number;
  dividendAmount: number;
  fiscalYear: number;
}

/**
 * Fiscal period for accounting
 */
export interface FiscalPeriodInfo {
  id: string;
  year: number;
  startDate: string;
  endDate: string;
  status: "OPEN" | "CLOSED" | "PENDING_CLOSE";
  closedBy?: string;
  closedAt?: string;
}

/**
 * Report parameters
 */
export interface ReportParams {
  reportType: string;
  startDate: string;
  endDate: string;
  format?: "PDF" | "EXCEL" | "CSV";
  memberId?: string;
  accountId?: string;
  loanId?: string;
}

/**
 * Report result
 */
export interface ReportResult {
  reportId: string;
  reportType: string;
  generatedAt: string;
  downloadUrl: string;
  expiresAt: string;
}

/**
 * Loan eligibility check result
 */
export interface LoanEligibility {
  eligible: boolean;
  maxLoanAmount: number;
  shareCapital: number;
  membershipMonths: number;
  activeLoansCount: number;
  reasons?: string[];
}

/**
 * Approval workflow step
 */
export interface ApprovalStep {
  id: string;
  loanId: string;
  step: number;
  role: UserRole;
  status: "PENDING" | "APPROVED" | "REJECTED";
  approvedBy?: string;
  approvedAt?: string;
  comments?: string;
}

/**
 * Notification preferences
 */
export interface NotificationPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  loanReminders: boolean;
  paymentReminders: boolean;
  dividendNotifications: boolean;
}

/**
 * Audit log entry (for admin view)
 */
export interface AuditLogEntry {
  id: string;
  userId: string;
  username: string;
  action: string;
  entityType: string;
  entityId?: string;
  description: string;
  ipAddress?: string;
  timestamp: string;
}

/**
 * System settings (admin only)
 */
export interface SystemSettings {
  maxLoanMultiplier: number;
  minMembershipMonthsForLoan: number;
  maxActiveLoansPerMember: number;
  defaultInterestRate: number;
  lateFeePercentage: number;
  dividendRate: number;
  fiscalYearStart: string;
  fiscalYearEnd: string;
}

/**
 * Dashboard widget configuration
 */
export interface WidgetConfig {
  id: string;
  type: string;
  title: string;
  visible: boolean;
  order: number;
  size: "small" | "medium" | "large";
}

/**
 * Recent activity item
 */
export interface ActivityItem {
  id: string;
  type: "LOAN" | "PAYMENT" | "DEPOSIT" | "WITHDRAWAL" | "MEMBER" | "SYSTEM";
  action: string;
  description: string;
  userId?: string;
  userName?: string;
  timestamp: string;
  metadata?: Record<string, any>;
}

/**
 * Search result item
 */
export interface SearchResult {
  type: "MEMBER" | "LOAN" | "ACCOUNT" | "PAYMENT";
  id: string;
  title: string;
  subtitle: string;
  url: string;
}

/**
 * Bulk operation result
 */
export interface BulkOperationResult {
  total: number;
  successful: number;
  failed: number;
  errors?: Array<{
    id: string;
    error: string;
  }>;
}

/**
 * File upload result
 */
export interface FileUploadResult {
  fileId: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  url: string;
  uploadedAt: string;
}

/**
 * Export types for TypeScript type inference
 */
export type UserRoleType = keyof typeof UserRole;
export type MemberStatusType = keyof typeof MemberStatus;
export type LoanTypeType = keyof typeof LoanType;
export type LoanStatusType = keyof typeof LoanStatus;
export type AccountTypeType = keyof typeof AccountType;
export type AccountStatusType = keyof typeof AccountStatus;
export type TransactionTypeType = keyof typeof TransactionType;
export type PaymentTypeType = keyof typeof PaymentType;
export type PaymentStatusType = keyof typeof PaymentStatus;
export type CollateralTypeType = keyof typeof CollateralType;
