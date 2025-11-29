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
  PRESIDENT = 'PRESIDENT',
  SECRETARY = 'SECRETARY',
  OFFICER = 'OFFICER',
  MEMBER = 'MEMBER'
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  token: string;
  expiresIn: number;
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
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
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
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
  EMERGENCY = 'EMERGENCY',
  EDUCATION = 'EDUCATION'
}

export enum LoanStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  DISBURSED = 'DISBURSED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED',
  DEFAULTED = 'DEFAULTED'
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
  REAL_ESTATE = 'REAL_ESTATE',
  VEHICLE = 'VEHICLE',
  JEWELRY = 'JEWELRY',
  EQUIPMENT = 'EQUIPMENT',
  OTHER = 'OTHER'
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
  REGULAR = 'REGULAR',
  SPECIAL = 'SPECIAL',
  FIXED_DEPOSIT = 'FIXED_DEPOSIT'
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  FROZEN = 'FROZEN',
  CLOSED = 'CLOSED'
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
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  INTEREST = 'INTEREST',
  FEE = 'FEE',
  TRANSFER_IN = 'TRANSFER_IN',
  TRANSFER_OUT = 'TRANSFER_OUT'
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
  PRINCIPAL = 'PRINCIPAL',
  INTEREST = 'INTEREST',
  PENALTY = 'PENALTY',
  FULL_PAYMENT = 'FULL_PAYMENT'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}

// API Response Types
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

// Utility Types
export interface SelectOption {
  value: string | number;
  label: string;
}

export interface TableColumn {
  id: string;
  label: string;
  minWidth?: number;
  align?: 'left' | 'center' | 'right';
  format?: (value: any) => string;
}

export interface FilterParams {
  page?: number;
  size?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  search?: string;
  status?: string;
  type?: string;
  memberId?: string;
  loanId?: string;
  startDate?: string;
  endDate?: string;
}
