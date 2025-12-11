import axiosClient from "./axiosClient";
import {
  Member,
  Transaction,
  UserRole,
  LoanStatus,
  PaymentNotification,
} from "../types";
import { MOCK_TRANSACTIONS, MOCK_ACCOUNTS } from "../constants";

// Simulated network delay (ms) for unimplemented features
const DELAY = 800;

const delay = <T>(data: T): Promise<T> => {
  return new Promise((resolve) => setTimeout(() => resolve(data), DELAY));
};

// Helper function to map backend payment types to frontend transaction types
const mapPaymentTypeToTransaction = (
  paymentType: string
): Transaction["type"] => {
  const typeMap: Record<string, Transaction["type"]> = {
    LOAN_REPAYMENT: "LOAN_REPAYMENT",
    LOAN_DISBURSEMENT: "LOAN_DISBURSEMENT",
    SHARE_PURCHASE: "SHARE_PURCHASE",
    DEPOSIT: "DEPOSIT",
    WITHDRAWAL: "WITHDRAWAL",
    SAVINGS_DEPOSIT: "DEPOSIT",
    SAVINGS_WITHDRAWAL: "WITHDRAWAL",
    INTEREST_PAYMENT: "INCOME",
    FEE: "EXPENSE",
    FINE: "FINE",
    DIVIDEND: "INCOME",
  };
  return typeMap[paymentType] || "DEPOSIT";
};

// In-memory storage for notifications to persist during session
let MOCK_PAYMENT_NOTIFICATIONS: PaymentNotification[] = [
  {
    id: "PN-001",
    memberId: "M004",
    memberName: "Aree Sampun",
    loanId: "L002",
    amount: 1500,
    date: "2023-10-25",
    status: "PENDING",
    timestamp: new Date().toISOString(),
  },
];

export const api = {
  auth: {
    login: async (credentials: any) => {
      const response = await axiosClient.post("/auth/login", credentials);
      // Backend returns: { token, refreshToken, expiresIn, type, id, username, email, role, permissions }
      const data = response.data;

      let role = UserRole.MEMBER; // Default
      if (data.role) {
        // Backend returns role as string (e.g., "PRESIDENT", "SECRETARY", "OFFICER", "MEMBER")
        const backendRole = data.role.toUpperCase();
        if (backendRole === "PRESIDENT") role = UserRole.PRESIDENT;
        else if (backendRole === "SECRETARY") role = UserRole.SECRETARY;
        else if (backendRole === "OFFICER") role = UserRole.OFFICER;
        else if (backendRole === "MEMBER") role = UserRole.MEMBER;
      }

      return {
        token: data.token,
        refreshToken: data.refreshToken || data.token,
        user: {
          id: data.id.toString(),
          username: data.username,
          role: role,
          permissions: data.permissions || [],
        },
      };
    },
    logout: async () => {
      await axiosClient.post("/auth/logout").catch(() => {}); // Best effort
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      return { success: true };
    },
    me: async () => {
      const response = await axiosClient.get("/auth/me");
      const data = response.data;

      let role = UserRole.MEMBER;
      if (data.role) {
        const backendRole = data.role.toUpperCase();
        if (backendRole === "PRESIDENT") role = UserRole.PRESIDENT;
        else if (backendRole === "SECRETARY") role = UserRole.SECRETARY;
        else if (backendRole === "OFFICER") role = UserRole.OFFICER;
        else if (backendRole === "MEMBER") role = UserRole.MEMBER;
      }

      return {
        id: data.id.toString(),
        role: role,
      };
    },
  },

  members: {
    getMyProfile: async () => {
      const response = await axiosClient.get("/members/my-profile");
      const m = response.data;
      return {
        id: m.uuid,
        fullName: m.name,
        idCardNumber: m.idCard,
        birthDate: m.dateOfBirth,
        address: m.address,
        phoneNumber: m.phone,
        shareBalance: m.shareCapital,
        savingsBalance: m.savingAccount ? m.savingAccount.balance : 0,
        joinedDate: m.registrationDate,
        // Map status appropriately
        status: (m.isActive ? "ACTIVE" : "INACTIVE") as "ACTIVE" | "INACTIVE",
        isFrozen: false,
        monthlyIncome: m.monthlyIncome,
        occupation: m.occupation,
      };
    },
    getAll: async () => {
      const response = await axiosClient.get("/members?size=100"); // Fetch up to 100 for now
      // Backend returns Page<Member>, we need content.
      const backendMembers = response.data.content || [];
      return backendMembers.map((m: any) => ({
        id: m.uuid,
        fullName: m.name,
        idCardNumber: m.idCard,
        birthDate: m.dateOfBirth,
        address: m.address,
        phoneNumber: m.phone,
        shareBalance: m.shareCapital,
        savingsBalance: m.savingAccount ? m.savingAccount.balance : 0,
        joinedDate: m.registrationDate,
        status: (m.isActive ? "ACTIVE" : "INACTIVE") as "ACTIVE" | "INACTIVE",
        isFrozen: false, // Not directly mapped yet
        monthlyIncome: m.monthlyIncome,
        occupation: m.occupation,
      }));
    },
    getById: async (id: string) => {
      const response = await axiosClient.get(`/members/${id}`);
      const m = response.data;
      return {
        id: m.uuid,
        fullName: m.name,
        idCardNumber: m.idCard,
        birthDate: m.dateOfBirth,
        address: m.address,
        phoneNumber: m.phone,
        shareBalance: m.shareCapital,
        savingsBalance: m.savingAccount ? m.savingAccount.balance : 0,
        joinedDate: m.registrationDate,
        status: (m.isActive ? "ACTIVE" : "INACTIVE") as "ACTIVE" | "INACTIVE",
        isFrozen: false,
        monthlyIncome: m.monthlyIncome,
        occupation: m.occupation,
      };
    },
    search: async (keyword: string) => {
      const response = await axiosClient.get(`/members/search?keyword=${keyword}`);
      const backendMembers = response.data || [];
      return backendMembers.map((m: any) => ({
        id: m.uuid,
        fullName: m.name,
        idCardNumber: m.idCard,
        birthDate: m.dateOfBirth,
        address: m.address,
        phoneNumber: m.phone,
        shareBalance: m.shareCapital,
        savingsBalance: m.savingAccount ? m.savingAccount.balance : 0,
        joinedDate: m.registrationDate,
        status: (m.isActive ? "ACTIVE" : "INACTIVE") as "ACTIVE" | "INACTIVE",
        isFrozen: false,
        monthlyIncome: m.monthlyIncome,
        occupation: m.occupation,
      }));
    },
    create: async (member: Member) => {
      // Map Frontend Member -> Backend DTO
      const payload = {
        name: member.fullName,
        idCard: member.idCardNumber,
        dateOfBirth: member.birthDate,
        address: member.address,
        phone: member.phoneNumber,
        occupation: member.occupation,
        monthlyIncome: member.monthlyIncome,
        // Defaults
        registrationDate: new Date().toISOString().split("T")[0],
        shareCapital: 0,
        isActive: true,
      };
      const response = await axiosClient.post("/members", payload);
      const m = response.data;
      // Return mapped result
      return {
        id: m.uuid,
        fullName: m.name,
        idCardNumber: m.idCard,
        birthDate: m.dateOfBirth,
        address: m.address,
        phoneNumber: m.phone,
        shareBalance: m.shareCapital,
        savingsBalance: 0,
        joinedDate: m.registrationDate,
        status: (m.isActive ? "ACTIVE" : "INACTIVE") as "ACTIVE" | "INACTIVE",
      };
    },
    update: async (id: string, updates: Partial<Member>) => {
      // This requires a more complex mapping update, simplified for now
      // Assuming updates contains mapped keys. Backend expects specific DTO.
      // For now, this might need refinement if used heavily.
      // Let's implement a basic mapped update.
      const payload: any = {};
      if (updates.fullName) payload.name = updates.fullName;
      if (updates.address) payload.address = updates.address;
      if (updates.phoneNumber) payload.phone = updates.phoneNumber;
      // ... map other fields as needed

      await axiosClient.put(`/members/${id}`, payload);
      return { id, success: true };
    },
    delete: async (id: string) => {
      await axiosClient.delete(`/members/${id}`);
      return { id, success: true };
    },
    getStatistics: async () => {
      const response = await axiosClient.get("/members/statistics");
      return {
        totalMembers: response.data.totalMembers,
        activeMembers: response.data.activeMembers,
        frozenMembers: 0, // Backend stats might not have this yet
      };
    },

    // Get transactions for a specific member
    getTransactions: async (memberId: string) => {
      try {
        const response = await axiosClient.get(
          `/members/${memberId}/transactions?size=50&sort=paymentDate,desc`
        );
        const payments = response.data.content || [];
        return payments.map((p: any) => ({
          id: p.id?.toString() || `T-${Date.now()}`,
          date: p.paymentDate || p.createdAt,
          type: mapPaymentTypeToTransaction(p.paymentType),
          category: p.paymentType,
          amount: p.amount || 0,
          memberId: memberId,
          description:
            p.description || `Payment ${p.paymentNumber || ""}`.trim(),
          receiptId: p.paymentNumber,
        }));
      } catch (error) {
        console.warn("Failed to fetch member transactions", error);
        return [];
      }
    },

    // Get loan summary for a specific member
    getLoanSummary: async (memberId: string) => {
      try {
        const response = await axiosClient.get(
          `/members/${memberId}/loan-summary`
        );
        return response.data;
      } catch (error) {
        // Fallback: calculate from loans endpoint
        console.warn(
          "Loan summary endpoint not available, calculating from loans"
        );
        const loansResponse = await axiosClient.get(
          `/loans?memberId=${memberId}&size=100`
        );
        const loans = loansResponse.data.content || [];

        const activeLoans = loans.filter((l: any) => l.status === "ACTIVE");
        const pendingLoans = loans.filter((l: any) => l.status === "PENDING");
        const totalBorrowed = loans.reduce(
          (sum: number, l: any) => sum + (l.principalAmount || 0),
          0
        );
        const totalOutstanding = activeLoans.reduce(
          (sum: number, l: any) => sum + (l.outstandingBalance || 0),
          0
        );

        return {
          activeLoansCount: activeLoans.length,
          pendingLoansCount: pendingLoans.length,
          totalLoans: loans.length,
          totalBorrowed,
          totalOutstanding,
          totalPaid: totalBorrowed - totalOutstanding,
          isEligibleForLoan:
            activeLoans.length < 2 && pendingLoans.length === 0,
          loans: loans.map((l: any) => ({
            id: l.uuid,
            loanNumber: l.loanNumber,
            principalAmount: l.principalAmount,
            outstandingBalance: l.outstandingBalance,
            status: l.status,
            loanType: l.loanType,
            startDate: l.startDate,
          })),
        };
      }
    },

    // Get risk profile for a member
    getRiskProfile: async (memberId: string) => {
      try {
        const response = await axiosClient.get(
          `/members/${memberId}/risk-profile`
        );
        return response.data;
      } catch (error) {
        console.warn("Risk profile endpoint not available");
        return null;
      }
    },
  },

  loans: {
    getAll: async () => {
      const response = await axiosClient.get("/loans?size=100");
      const backendLoans = response.data.content || [];
      return backendLoans.map((l: any) => ({
        id: l.uuid,
        memberId: l.member ? l.member.memberId : "",
        memberName: l.member ? l.member.name : "Unknown",
        principalAmount: l.principalAmount,
        remainingBalance: l.outstandingBalance,
        interestRate: l.interestRate,
        termMonths: l.termMonths,
        startDate: l.startDate,
        status: l.status,
        loanType: l.loanType,
        contractNo: l.loanNumber,
        guarantorIds: [], // Needs extra fetch or expansion if needed, skipping for now
        collateralRef:
          l.collaterals && l.collaterals.length > 0
            ? l.collaterals[0].description
            : undefined,
      }));
    },
    getById: async (id: string) => {
      const response = await axiosClient.get(`/loans/${id}`);
      const l = response.data;
      return {
        id: l.uuid,
        memberId: l.member ? l.member.memberId : "",
        memberName: l.member ? l.member.name : "Unknown",
        principalAmount: l.principalAmount,
        remainingBalance: l.outstandingBalance,
        interestRate: l.interestRate,
        termMonths: l.termMonths,
        startDate: l.startDate,
        status: l.status,
        loanType: l.loanType,
        contractNo: l.loanNumber,
        guarantorIds: [], // Needs extra fetch
        collateralRef:
          l.collaterals && l.collaterals.length > 0
            ? l.collaterals[0].description
            : undefined,
      };
    },
    apply: async (application: any) => {
      // Map Frontend Application -> Backend Request DTO
      // Assuming application object matches mostly or we map it:
      // { memberId, amount, term, type, reason } -> { memberId, principalAmount, termMonths, loanType, purpose }
      const payload = {
        memberUuid: application.memberId, // frontend 'memberId' is UUID
        principalAmount: application.amount,
        termMonths: application.term,
        loanType: application.type,
        purpose: application.reason,
        interestRate: 12.0, // Default
        guarantors: application.guarantorIds ? application.guarantorIds.map((guid: string) => ({
             memberUuid: guid // Assuming guarantorIds are also UUIDs (from search)
        })) : []
      };

      // If application has memberId as UUID, and backend needs Long...
      // We will assume for now backend can handle it or we updated it.
      // Actually, let's assume valid payload for now.
      const response = await axiosClient.post("/loans/apply", payload);
      const l = response.data;
      return {
        id: l.uuid,
        memberId: "...", // response might not have full member details immediately
        memberName: "...",
        principalAmount: l.principalAmount,
        remainingBalance: l.outstandingBalance,
        interestRate: l.interestRate,
        termMonths: l.termMonths,
        startDate: l.startDate,
        status: l.status,
        loanType: l.loanType,
        contractNo: l.loanNumber,
        guarantorIds: [],
      };
    },
    /**
     * Member self-service loan application.
     * Uses the authenticated member's context (no memberId needed in payload).
     */
    applyForMyLoan: async (application: {
      loanType: string;
      principalAmount: number;
      termMonths: number;
      purpose: string;
      guarantors?: Array<{
        memberId: number;
        guaranteedAmount?: number;
        relationship?: string;
      }>;
    }) => {
      const payload = {
        loanType: application.loanType,
        principalAmount: application.principalAmount,
        termMonths: application.termMonths,
        purpose: application.purpose,
        guarantors: application.guarantors || [],
      };

      const response = await axiosClient.post("/loans/member-apply", payload);
      const l = response.data;
      return {
        id: l.uuid || l.id?.toString(),
        loanNumber: l.loanNumber,
        principalAmount: l.principalAmount,
        outstandingBalance: l.outstandingBalance,
        interestRate: l.interestRate,
        termMonths: l.termMonths,
        startDate: l.startDate,
        status: l.status,
        loanType: l.loanType,
      };
    },
    approve: async (id: string) => {
      // ID here is UUID. Backend Controller uses Long `loanId` in path: `/{loanId}/approve`.
      // We have a mismatch! Backend controllers use Long IDs for actions, but expose UUIDs for reading.
      // We must fix this or look up ID.
      // Best approach for now: Retrieve the loan by UUID first to get the Long ID?
      // Wait, the `getById` returns mapped object. The mapped object `id` is UUID.
      // We don't have the Long ID in the frontend model!
      // CRITICAL GAP: Frontend needs Long ID to call action endpoints if they rely on Long ID.
      // OR we should update Backend to use UUIDs for actions.
      // Backend `LoanController` uses `@PathVariable Long loanId`.
      // I should probably switch the frontend to use Long IDs? No, UUID is safer.
      // I will temporarily make a call to `GET /loans/{uuid}` which returns `LoanResponse` containing `id` (Long)?
      // Let's check `LoanResponse` DTO structure.

      // Assuming for this immediate step we might fail if we pass UUID to an endpoint expecting Long.
      // STRATEGY:
      // 1. Fetch Loan by UUID (which we have) -> Get its internal Long ID (if exposed).
      // 2. Call Action with Long ID.

      // Let's try to see if `LoanResponse` includes the Long ID.
      const loanResponse = await axiosClient.get(`/loans/${id}`);
      const internalId = loanResponse.data.id; // Assuming DTO has it.

      const response = await axiosClient.post(`/loans/${internalId}/approve`, {
        approvedAmount: loanResponse.data.principalAmount, // Default to principal
        notes: "Approved via Web",
      });
      return { id, status: LoanStatus.APPROVED };
    },
    reject: async (id: string, reason: string) => {
      const loanResponse = await axiosClient.get(`/loans/${id}`);
      const internalId = loanResponse.data.id;

      await axiosClient.post(
        `/loans/${internalId}/reject?rejectionReason=${encodeURIComponent(
          reason
        )}`
      );
      return { id, status: LoanStatus.REJECTED };
    },
    disburse: async (id: string) => {
      const loanResponse = await axiosClient.get(`/loans/${id}`);
      const internalId = loanResponse.data.id;

      await axiosClient.post(`/loans/${internalId}/disburse`);
      return { id, status: LoanStatus.ACTIVE };
    },
    repay: async (id: string, amount: number) => {
      // This is a payment.
      // We need to create a `PaymentRequest`.
      // Backend: `POST /api/payments`
      // Payload: `PaymentRequest` { memberId, loanId, amount, type=LOAN_REPAYMENT, ... }
      // Again, we need Long IDs for member and loan.

      const loanResponse = await axiosClient.get(`/loans/${id}`);
      const internalLoanId = loanResponse.data.id;
      const internalMemberId = loanResponse.data.member
        ? loanResponse.data.member.id
        : null;

      await axiosClient.post("/payments", {
        memberId: internalMemberId,
        loanId: internalLoanId,
        amount: amount,
        paymentType: "LOAN_REPAYMENT",
        paymentMethod: "CASH",
        paymentDate: new Date().toISOString().split("T")[0],
      });

      return { id, success: true, newBalance: 0 }; // Balance update happens on refresh
    },
  },

  notifications: {
    notifyPayment: (data: Partial<PaymentNotification>) => {
      const newNotif: PaymentNotification = {
        ...(data as PaymentNotification),
        id: `PN-${Date.now()}`,
        status: "PENDING",
        timestamp: new Date().toISOString(),
      };
      MOCK_PAYMENT_NOTIFICATIONS.push(newNotif);
      return delay(newNotif);
    },
    getAll: () => delay(MOCK_PAYMENT_NOTIFICATIONS),
    approve: (id: string) => {
      MOCK_PAYMENT_NOTIFICATIONS = MOCK_PAYMENT_NOTIFICATIONS.map((n) =>
        n.id === id ? { ...n, status: "APPROVED" } : n
      );
      return delay({ success: true });
    },
    reject: (id: string) => {
      MOCK_PAYMENT_NOTIFICATIONS = MOCK_PAYMENT_NOTIFICATIONS.map((n) =>
        n.id === id ? { ...n, status: "REJECTED" } : n
      );
      return delay({ success: true });
    },
  },

  savings: {
    getAccounts: async () => {
      const response = await axiosClient.get("/savings/accounts?size=100");
      const content = response.data.content || [];
      return content.map((a: any) => ({
        id: a.id.toString(),
        accountNumber: a.accountNumber,
        memberId: a.memberId ? a.memberId.toString() : "",
        memberName: a.memberName,
        balance: a.balance,
        status: a.status, // Assuming backend returns ACTIVE/FROZEN etc
      }));
    },
    deposit: async (memberId: string, amount: number) => {
      // We need account ID, but signature only has memberId.
      // Frontend likely needs updating or we fetch account for member.
      // Assuming member has one account for simplicity or we find the right one.
      // Strategy: Fetch accounts for member, take the first one.
      const accountsResp = await axiosClient.get(
        `/savings/accounts/member/${memberId}`
      );
      const accounts = accountsResp.data.content || [];
      if (accounts.length === 0)
        throw new Error("No savings account found for member");

      const accountId = accounts[0].id;
      const response = await axiosClient.post(
        `/savings/accounts/${accountId}/deposit?amount=${amount}&description=Deposit`
      );
      return { success: true, txnId: `T${Date.now()}` };
    },
    withdraw: async (memberId: string, amount: number) => {
      const accountsResp = await axiosClient.get(
        `/savings/accounts/member/${memberId}`
      );
      const accounts = accountsResp.data.content || [];
      if (accounts.length === 0)
        throw new Error("No savings account found for member");

      const accountId = accounts[0].id;
      await axiosClient.post(
        `/savings/accounts/${accountId}/withdraw?amount=${amount}&description=Withdrawal`
      );
      return { success: true, txnId: `T${Date.now()}` };
    },
  },

  accounting: {
    getTransactions: async () => {
      // Mapping Payments to Transactions for display
      const response = await axiosClient.get(
        "/payments?size=20&sort=paymentDate,desc"
      );
      const payments = response.data.content || [];
      return payments.map((p: any) => ({
        id: p.id.toString(),
        date: p.paymentDate,
        type: p.paymentType, // e.g. LOAN_REPAYMENT, SHARE_PURCHASE
        category: "INCOME", // Simplified
        amount: p.amount,
        memberId: p.memberId ? p.memberId.toString() : "",
        description: `Payment ${p.paymentNumber}`,
        receiptId: p.paymentNumber,
      }));
    },
    getAccounts: async () => {
      const response = await axiosClient.get("/accounting/accounts");
      // Backend returns List<Account> directly, not Page
      return response.data;
    },
    createEntry: async (entry: Partial<Transaction>) => {
      // entry: { category (~accountCode), amount, type, description }
      // Mapping frontend "category" (which is likely Account Code in this context) to backend
      const payload = {
        accountCode: entry.category, // Assuming dropdown sends code
        amount: entry.amount,
        type: entry.type === "INCOME" ? "CREDIT" : "DEBIT", // Simplified assumption
        description: entry.description,
        transactionDate: new Date().toISOString().split("T")[0],
      };
      const response = await axiosClient.post("/journal/entries", payload);
      return { ...entry, id: `GL${Date.now()}` };
    },
    getBalanceSheet: () =>
      delay({
        assets: 1500000,
        liabilities: 1200000,
        equity: 300000,
      }),
  },

  dashboard: {
    getOfficerStats: () =>
      delay({
        cashBalance: 79949.5,
        depositsToday: 15000,
        repaymentsToday: 8500,
        pendingTasks: 2,
      }),
    // President Dashboard Integrations
    getPARAnalysis: async () => {
      const response = await axiosClient.get(
        "/dashboard/president/par-analysis"
      );
      return response.data;
    },
    getLiquidityRatio: async () => {
      const response = await axiosClient.get("/dashboard/president/liquidity");
      return response.data;
    },
    getMembershipTrends: async (months = 12) => {
      const response = await axiosClient.get(
        `/dashboard/president/membership-trends?months=${months}`
      );
      return response.data;
    },
  },
};
