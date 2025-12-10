export enum UserRole {
  PRESIDENT = "PRESIDENT",
  SECRETARY = "SECRETARY",
  OFFICER = "OFFICER",
  MEMBER = "MEMBER",
}

export enum LoanStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED", // Approved but not yet disbursed
  ACTIVE = "ACTIVE", // Disbursed and currently accruing interest
  REJECTED = "REJECTED",
  PAID = "PAID",
  DEFAULTED = "DEFAULTED",
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: {
    id: string;
    username: string;
    role: UserRole;
    permissions: string[];
  };
}

export interface Member {
  id: string;
  fullName: string;
  idCardNumber?: string;
  birthDate: string; // ISO date string
  address: string;
  phoneNumber: string;
  shareBalance: number;
  savingsBalance: number;
  joinedDate: string;
  status: "ACTIVE" | "INACTIVE";
  isFrozen?: boolean;
  monthlyIncome?: number;
  occupation?: string;
}

export interface CollateralDocument {
  id: string;
  type: "DEED" | "VEHICLE_BOOK" | "GUARANTOR_ID" | "CONTRACT" | "OTHER";
  description: string;
  url: string; // URL or Base64
  fileName: string;
  fileSize?: number;
  uploadedAt: string;
}

export interface Loan {
  id: string;
  memberId: string;
  memberName: string;
  principalAmount: number;
  remainingBalance: number;
  interestRate: number;
  termMonths: number;
  startDate: string;
  status: LoanStatus;
  loanType: "EMERGENCY" | "COMMON" | "INVESTMENT";
  contractNo?: string;
  guarantorIds: string[];
  collateralRef?: string;
  documents?: CollateralDocument[];
}

export interface Transaction {
  id: string;
  date: string;
  type:
    | "DEPOSIT"
    | "WITHDRAWAL"
    | "LOAN_DISBURSEMENT"
    | "LOAN_REPAYMENT"
    | "SHARE_PURCHASE"
    | "INCOME"
    | "EXPENSE"
    | "FINE";
  category?: string;
  amount: number;
  memberId?: string;
  description: string;
  receiptId?: string;
}

export interface Account {
  code: string;
  name: string;
  category: "ASSET" | "LIABILITY" | "EQUITY" | "REVENUE" | "EXPENSE";
  balance: number;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  date: string;
  type: "INFO" | "SUCCESS" | "ALERT";
  read: boolean;
}

export interface PaymentNotification {
  id: string;
  memberId: string;
  memberName: string;
  loanId: string;
  amount: number;
  date: string;
  slipUrl?: string; // Mocked URL or Base64
  status: "PENDING" | "APPROVED" | "REJECTED";
  timestamp: string;
}

export interface CashReconciliation {
  id: string;
  date: string;
  officerId: string;
  physicalCount: Record<string, number>; // denomination -> count
  totalPhysical: number;
  systemBalance: number;
  difference: number;
  status: "PENDING" | "APPROVED" | "REJECTED";
  notes?: string;
}

export interface DividendDistribution {
  year: number;
  totalProfit: number;
  dividendRate: number;
  avgReturnRate: number;
  totalDistributed: number;
  status: "PROPOSED" | "APPROVED" | "DISTRIBUTED";
}

export interface LiquidityDTO {
  liquidityRatio: number;
  status: "HEALTHY" | "WARNING" | "CRITICAL";
  cashAndBank: number;
  totalSavings: number;
}

export interface PARAnalysisDTO {
  parRatio: number;
  totalPortfolio: number;
  par1to30: number;
  par31to60: number;
  par61to90: number;
  parOver90: number;
}

export interface MembershipTrendsDTO {
  labels: string[];
  newMembers: number[];
  totalMembers: number[];
}

// Member Loan Summary from backend
export interface MemberLoanSummary {
  activeLoansCount: number;
  pendingLoansCount: number;
  totalLoans: number;
  totalBorrowed: number;
  totalOutstanding: number;
  totalPaid: number;
  isEligibleForLoan: boolean;
  loans?: MemberLoanItem[];
}

export interface MemberLoanItem {
  id: string;
  loanNumber: string;
  principalAmount: number;
  outstandingBalance: number;
  status: string;
  loanType: string;
  startDate: string;
}

// Member Risk Profile
export interface MemberRiskProfile {
  memberId: number;
  memberName: string;
  totalDebt: number;
  totalGuaranteed: number;
  netLiability: number;
  riskStatus: "LOW" | "MEDIUM" | "HIGH";
}

export type ViewState =
  | "DASHBOARD"
  | "MEMBERS"
  | "LOANS"
  | "ACCOUNTING"
  | "ASSISTANT";
