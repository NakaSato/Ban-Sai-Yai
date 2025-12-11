import React, { useEffect, useState } from "react";
import {
  Building2,
  User,
  PiggyBank,
  TrendingUp,
  Receipt,
  ChevronRight,
  FileText,
  Clock,
  CheckCircle,
  Users,
  Shield,
  History,
  ArrowDownLeft,
  ArrowUpRight,
  Loader2,
  AlertTriangle,
  Info,
  Calendar,
  X,
  CreditCard,
  Send,
} from "lucide-react";
import { api } from "../../services/api";
import {
  LoanStatus,
  Member,
  Loan,
  Transaction,
  Notification,
} from "../../types";

// Simple Loan Application Modal for Members
interface MemberLoanModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (form: LoanApplicationForm) => Promise<void>;
  isSubmitting: boolean;
}

interface LoanApplicationForm {
  loanType: string;
  principalAmount: number;
  termMonths: number;
  purpose: string;
}

const MemberLoanModal: React.FC<MemberLoanModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  isSubmitting,
}) => {
  const [form, setForm] = useState<LoanApplicationForm>({
    loanType: "PERSONAL",
    principalAmount: 10000,
    termMonths: 12,
    purpose: "",
  });
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    setError("");
    if (!form.purpose || form.purpose.length < 10) {
      setError("Please provide a purpose (at least 10 characters)");
      return;
    }
    if (form.principalAmount < 1000) {
      setError("Minimum loan amount is ฿1,000");
      return;
    }
    try {
      await onSubmit(form);
      onClose();
    } catch (e: any) {
      setError(e.message || "Failed to submit loan application");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden">
        {/* Header */}
        <div className="px-6 py-5 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-emerald-100 text-emerald-600 rounded-xl">
              <CreditCard className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900">
                Apply for Loan
              </h3>
              <p className="text-xs text-gray-500">
                Submit your loan application
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-full hover:bg-white transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <div className="p-6 space-y-5">
          {/* Loan Type */}
          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">
              Loan Type
            </label>
            <div className="grid grid-cols-2 gap-2">
              {["PERSONAL", "EMERGENCY", "BUSINESS", "EDUCATION"].map(
                (type) => (
                  <button
                    key={type}
                    onClick={() => setForm((f) => ({ ...f, loanType: type }))}
                    className={`py-2.5 px-3 rounded-xl text-sm font-medium transition-all ${
                      form.loanType === type
                        ? "bg-emerald-600 text-white shadow-md"
                        : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                    }`}
                  >
                    {type.charAt(0) + type.slice(1).toLowerCase()}
                  </button>
                )
              )}
            </div>
          </div>

          {/* Amount */}
          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">
              Amount (฿)
            </label>
            <input
              type="number"
              min="1000"
              max="500000"
              step="1000"
              value={form.principalAmount}
              onChange={(e) =>
                setForm((f) => ({
                  ...f,
                  principalAmount: Number(e.target.value),
                }))
              }
              className="w-full bg-gray-50 border-2 border-transparent rounded-xl px-4 py-3 text-lg font-bold text-emerald-700 focus:border-emerald-500 focus:bg-white outline-none transition"
            />
            <p className="text-xs text-gray-400 mt-1">
              Min: ฿1,000 - Max: ฿500,000
            </p>
          </div>

          {/* Term */}
          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">
              Repayment Term
            </label>
            <select
              value={form.termMonths}
              onChange={(e) =>
                setForm((f) => ({ ...f, termMonths: Number(e.target.value) }))
              }
              className="w-full bg-gray-50 border-2 border-transparent rounded-xl px-4 py-3 font-medium text-gray-700 focus:border-emerald-500 focus:bg-white outline-none transition"
            >
              <option value={6}>6 Months</option>
              <option value={12}>12 Months</option>
              <option value={24}>24 Months</option>
              <option value={36}>36 Months</option>
              <option value={48}>48 Months</option>
              <option value={60}>60 Months</option>
            </select>
          </div>

          {/* Purpose */}
          <div>
            <label className="block text-sm font-bold text-gray-700 mb-2">
              Purpose
            </label>
            <textarea
              rows={3}
              value={form.purpose}
              onChange={(e) =>
                setForm((f) => ({ ...f, purpose: e.target.value }))
              }
              placeholder="Please describe the reason for this loan..."
              className="w-full bg-gray-50 border-2 border-transparent rounded-xl px-4 py-3 text-sm text-gray-700 focus:border-emerald-500 focus:bg-white outline-none transition resize-none"
            />
            <p className="text-xs text-gray-400 mt-1">
              {form.purpose.length}/500 characters (min 10)
            </p>
          </div>

          {/* Error */}
          {error && (
            <div className="flex items-center gap-2 text-red-600 bg-red-50 p-3 rounded-xl">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <p className="text-sm font-medium">{error}</p>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-100 bg-gray-50/50 flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 py-3 text-sm font-bold text-gray-600 hover:text-gray-800 rounded-xl transition"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="flex-1 bg-emerald-600 text-white py-3 rounded-xl text-sm font-bold hover:bg-emerald-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Submitting...
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                Submit Application
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

const MemberDashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [myData, setMyData] = useState<Member | null>(null);
  const [myLoans, setMyLoans] = useState<Loan[]>([]);
  const [guaranteedLoans, setGuaranteedLoans] = useState<Loan[]>([]);
  const [myTransactions, setMyTransactions] = useState<Transaction[]>([]);
  const [alerts, setAlerts] = useState<Notification[]>([]);

  // Loan Application Modal State
  const [showLoanModal, setShowLoanModal] = useState(false);
  const [isSubmittingLoan, setIsSubmittingLoan] = useState(false);

  const myMemberId = "M001"; // Mock logged in user

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [member, loans, transactions, notifications] = await Promise.all([
          api.members.getById(myMemberId),
          api.loans.getAll(),
          api.accounting.getTransactions(),
          api.notifications.getAll(), // Note: Using getPaymentNotifications mock for now, ideally fetch User Notifications
        ]);

        setMyData(member || null);
        setMyLoans(loans.filter((l) => l.memberId === myMemberId));

        // Filter loans where I am a guarantor
        setGuaranteedLoans(
          loans.filter(
            (l) =>
              l.guarantorIds.includes(myMemberId) &&
              (l.status === "ACTIVE" || l.status === "DEFAULTED")
          )
        );

        setMyTransactions(
          transactions.filter((t) => t.memberId === myMemberId)
        );

        // Mock fetching alerts from constants for demo
        // In real app: api.notifications.getUserNotifications(myMemberId)
        const mockAlerts = [
          {
            id: "N1",
            title: "Loan Approved",
            message: "Your common loan application (L003) has been approved.",
            date: "2 mins ago",
            type: "SUCCESS",
            read: false,
          },
          {
            id: "N2",
            title: "Payment Reminder",
            message: "Monthly loan repayment is due tomorrow.",
            date: "1 hour ago",
            type: "ALERT",
            read: false,
          },
        ] as Notification[];
        setAlerts(mockAlerts.filter((n) => !n.read));
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Find Active and Pending Loans
  const myActiveLoan = myLoans.find(
    (l) =>
      l.status === LoanStatus.APPROVED ||
      l.status === LoanStatus.ACTIVE ||
      l.status === LoanStatus.DEFAULTED
  );
  const myPendingLoan = myLoans.find((l) => l.status === LoanStatus.PENDING);

  const myLoanBalance = myActiveLoan ? myActiveLoan.remainingBalance : 0;
  const myTotalWealth =
    (myData?.savingsBalance || 0) + (myData?.shareBalance || 0);

  // Calculate Tenure
  const getTenure = (joinedDate?: string) => {
    if (!joinedDate) return "N/A";
    const start = new Date(joinedDate);
    const now = new Date();
    const years = now.getFullYear() - start.getFullYear();
    return `${years} Year${years !== 1 ? "s" : ""}`;
  };

  // Handle Loan Application Submission
  const handleLoanSubmit = async (form: LoanApplicationForm) => {
    setIsSubmittingLoan(true);
    try {
      const result = await api.loans.applyForMyLoan({
        loanType: form.loanType,
        principalAmount: form.principalAmount,
        termMonths: form.termMonths,
        purpose: form.purpose,
      });

      // Add success alert
      setAlerts((prev) => [
        {
          id: `N-${Date.now()}`,
          title: "Loan Application Submitted",
          message: `Your ${form.loanType.toLowerCase()} loan application for ฿${form.principalAmount.toLocaleString()} has been submitted and is pending review.`,
          date: "Just now",
          type: "SUCCESS",
          read: false,
        },
        ...prev,
      ]);

      // Refresh loans list
      const loans = await api.loans.getAll();
      setMyLoans(loans.filter((l) => l.memberId === myMemberId));

      setShowLoanModal(false);
    } catch (error: any) {
      console.error("Loan application failed:", error);
      throw new Error(
        error.response?.data?.message ||
          "Failed to submit loan application. Please try again."
      );
    } finally {
      setIsSubmittingLoan(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <Loader2 className="w-10 h-10 text-emerald-600 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in -mx-4 -mt-4 md:-mx-6 md:-mt-6 lg:-mx-8 lg:-mt-8 px-4 pt-4 md:px-6 md:pt-6 lg:px-8 lg:pt-8 pb-6">
      {/* Alerts Banner */}
      {alerts.length > 0 && (
        <div className="space-y-2">
          {alerts.map((alert) => (
            <div
              key={alert.id}
              className={`p-4 rounded-2xl flex items-start gap-3 shadow-sm ${
                alert.type === "SUCCESS"
                  ? "bg-emerald-50 text-emerald-800 border border-emerald-100"
                  : "bg-orange-50 text-orange-800 border border-orange-100"
              }`}
            >
              <div
                className={`p-1.5 rounded-full shrink-0 ${
                  alert.type === "SUCCESS" ? "bg-emerald-100" : "bg-orange-100"
                }`}
              >
                {alert.type === "SUCCESS" ? (
                  <CheckCircle className="w-4 h-4" />
                ) : (
                  <AlertTriangle className="w-4 h-4" />
                )}
              </div>
              <div className="flex-1">
                <h4 className="font-bold text-sm">{alert.title}</h4>
                <p className="text-xs opacity-90 mt-0.5">{alert.message}</p>
              </div>
              <span className="text-[10px] opacity-60 font-medium whitespace-nowrap">
                {alert.date}
              </span>
            </div>
          ))}
        </div>
      )}

      {/* Zone A: Member Identity (Hero Card) */}
      <div className="bg-gradient-to-br from-emerald-900 to-emerald-700 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden group">
        <div className="absolute top-0 right-0 p-4 opacity-10">
          <Building2 className="w-32 h-32" />
        </div>
        <div className="flex items-center space-x-4 relative z-10">
          <div className="w-16 h-16 bg-white/20 backdrop-blur-sm rounded-full border-2 border-emerald-100/50 flex items-center justify-center text-white shadow-inner shrink-0">
            <User className="w-8 h-8" />
          </div>
          <div>
            <p className="text-emerald-100 text-xs font-medium mb-0.5 uppercase tracking-wider">
              Member Profile
            </p>
            <h2 className="text-2xl font-bold leading-tight">
              {myData?.fullName}
            </h2>
            <div className="flex flex-wrap items-center mt-3 gap-2">
              <span className="bg-black/20 px-2 py-1 rounded-lg text-xs text-emerald-100 border border-emerald-500/30 backdrop-blur-sm flex items-center gap-1">
                <User className="w-3 h-3" /> ID: {myData?.id}
              </span>
              <span className="bg-black/20 px-2 py-1 rounded-lg text-xs text-emerald-100 border border-emerald-500/30 backdrop-blur-sm flex items-center gap-1">
                <Calendar className="w-3 h-3" /> Since:{" "}
                {new Date(myData?.joinedDate || "").getFullYear()}
              </span>
              <span className="bg-emerald-400/20 px-2 py-1 rounded-lg text-xs text-emerald-100 font-bold border border-emerald-400/30 backdrop-blur-sm">
                {getTenure(myData?.joinedDate)} Tenure
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Zone B: Digital Passbook (Green & Red Cards) */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Savings Card (Green) */}
        <div className="bg-gradient-to-br from-emerald-500 to-teal-600 rounded-3xl p-6 text-white shadow-lg relative overflow-hidden group transition-all hover:shadow-emerald-500/25 hover:shadow-xl">
          <div className="absolute right-[-10px] top-[-10px] opacity-20 transform group-hover:scale-110 transition-transform duration-500">
            <PiggyBank className="w-32 h-32" />
          </div>
          <div className="relative z-10 flex flex-col h-full justify-between min-h-[140px]">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <div className="p-1.5 bg-white/20 rounded-lg backdrop-blur-md">
                  <TrendingUp className="w-4 h-4 text-white" />
                </div>
                <span className="text-emerald-100 text-sm font-bold uppercase tracking-wider">
                  My Assets
                </span>
              </div>
              <h3 className="text-4xl font-bold tracking-tight mt-2">
                ฿{myTotalWealth.toLocaleString()}
              </h3>
            </div>
            <div className="mt-4 pt-4 border-t border-white/10 flex justify-between items-end">
              <p className="text-xs text-emerald-100 font-medium">
                Accumulated Savings & Shares
              </p>
              <span className="bg-white/20 px-2 py-1 rounded text-[10px] font-bold">
                +3.5% Div
              </span>
            </div>
          </div>
        </div>

        {/* Debt Card (Red) - Visual Distinction for Liability */}
        <div className="bg-gradient-to-br from-red-600 to-rose-700 rounded-3xl p-6 text-white shadow-lg relative overflow-hidden group transition-all hover:shadow-red-500/25 hover:shadow-xl">
          <div className="absolute right-[-10px] top-[-10px] opacity-20 transform group-hover:scale-110 transition-transform duration-500">
            <Receipt className="w-32 h-32" />
          </div>
          <div className="relative z-10 flex flex-col h-full justify-between min-h-[140px]">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <div className="p-1.5 bg-white/20 rounded-lg backdrop-blur-md">
                  <AlertTriangle className="w-4 h-4 text-white" />
                </div>
                <span className="text-red-100 text-sm font-bold uppercase tracking-wider">
                  My Liability
                </span>
              </div>
              <h3 className="text-4xl font-bold tracking-tight mt-2">
                ฿{myLoanBalance.toLocaleString()}
              </h3>
            </div>

            <div className="mt-4 pt-4 border-t border-white/10">
              {myActiveLoan ? (
                <div className="flex justify-between items-center">
                  <span className="text-xs text-red-100 font-medium">
                    Next Due: 30 Oct
                  </span>
                  <button className="bg-white text-red-600 px-3 py-1.5 rounded-lg text-xs font-bold shadow-sm hover:bg-red-50 transition flex items-center">
                    Pay Now <ChevronRight className="w-3 h-3 ml-1" />
                  </button>
                </div>
              ) : (
                <p className="text-xs text-red-100 font-medium flex items-center gap-1">
                  <CheckCircle className="w-3 h-3" /> Debt Free
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Zone C: Guarantees Provided (Transparency) */}
      {guaranteedLoans.length > 0 && (
        <div className="bg-white rounded-3xl p-5 shadow-sm border border-orange-100">
          <div className="flex items-center gap-2 mb-4 text-orange-800">
            <Shield className="w-5 h-5" />
            <h3 className="font-bold text-sm uppercase tracking-wide">
              Guarantees Provided
            </h3>
          </div>
          <div className="space-y-3">
            {guaranteedLoans.map((loan) => (
              <div
                key={loan.id}
                className="flex items-center justify-between p-3 bg-orange-50/50 rounded-xl border border-orange-100"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center font-bold text-xs border border-orange-200">
                    {loan.memberName.charAt(0)}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-gray-800">
                      Borrower: {loan.memberName}
                    </p>
                    <p className="text-[10px] text-gray-500">
                      Contract: {loan.contractNo || "Pending"}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-xs font-bold text-orange-700">
                    ฿{loan.remainingBalance.toLocaleString()}
                  </p>
                  <p className="text-[10px] text-orange-400">
                    Contingent Liability
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Zone D: Active Contracts & Status */}
      <div>
        <h3 className="text-gray-800 font-bold mb-4 flex items-center text-sm uppercase tracking-wide opacity-70">
          <FileText className="w-4 h-4 mr-2" /> Loan Applications
        </h3>

        {/* Scenario 1: Pending Application (Stepper) */}
        {myPendingLoan && (
          <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 mb-4">
            <div className="flex justify-between items-center mb-4">
              <span className="bg-yellow-100 text-yellow-800 text-xs font-bold px-2 py-1 rounded-full flex items-center gap-1">
                <Clock className="w-3 h-3" /> Application Pending
              </span>
              <span className="text-xs text-gray-400 font-mono">
                {myPendingLoan.id}
              </span>
            </div>

            {/* Stepper */}
            <div className="relative flex items-center justify-between text-xs text-gray-500 mb-4 px-2">
              <div className="absolute left-0 right-0 top-3 h-0.5 bg-gray-100 -z-10" />
              <div className="flex flex-col items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-emerald-600 text-white flex items-center justify-center shadow-sm ring-4 ring-white">
                  <CheckCircle className="w-3 h-3" />
                </div>
                <span className="font-medium text-emerald-600">Sent</span>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div
                  className={`w-6 h-6 rounded-full flex items-center justify-center shadow-sm ring-4 ring-white ${
                    myPendingLoan.guarantorIds.length > 0
                      ? "bg-blue-600 text-white"
                      : "bg-gray-200"
                  }`}
                >
                  {myPendingLoan.guarantorIds.length > 0 ? (
                    <CheckCircle className="w-3 h-3" />
                  ) : (
                    <Users className="w-3 h-3" />
                  )}
                </div>
                <span
                  className={
                    myPendingLoan.guarantorIds.length > 0
                      ? "text-blue-600 font-medium"
                      : ""
                  }
                >
                  Guarantor
                </span>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-gray-200 text-white flex items-center justify-center shadow-sm ring-4 ring-white">
                  <div className="w-2 h-2 bg-gray-400 rounded-full" />
                </div>
                <span>Result</span>
              </div>
            </div>
            <div className="bg-gray-50 rounded-lg p-3 text-center">
              <p className="text-[10px] text-gray-500">
                Estimated review completion: <strong>3-5 days</strong>
              </p>
            </div>
          </div>
        )}

        {/* Scenario 2: Active Loan Contract */}
        {myActiveLoan ? (
          <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 space-y-4">
            <div className="flex justify-between items-start border-b border-gray-100 pb-3">
              <div>
                <p className="text-gray-400 text-[10px] uppercase font-bold tracking-wider">
                  Contract No.
                </p>
                <p className="font-mono text-emerald-800 font-bold">
                  {myActiveLoan.contractNo || "CNT-XX-XXX"}
                </p>
              </div>
              <div className="text-right">
                <p className="text-gray-400 text-[10px] uppercase font-bold tracking-wider">
                  Type
                </p>
                <p className="text-gray-800 font-medium text-sm">
                  {myActiveLoan.loanType}
                </p>
              </div>
            </div>

            {/* Guarantor Transparency Widget */}
            <div className="bg-gray-50 rounded-xl p-3 border border-gray-100">
              <p className="text-xs font-bold text-gray-600 mb-2 flex items-center">
                <Shield className="w-3 h-3 mr-1.5 text-gray-400" /> My
                Guarantors
              </p>
              <div className="flex flex-wrap gap-2">
                {myActiveLoan.guarantorIds.map((gid) => {
                  return (
                    <div
                      key={gid}
                      className="flex items-center bg-white px-2 py-1.5 rounded-lg border border-gray-200 shadow-sm"
                    >
                      <div className="w-5 h-5 bg-purple-100 rounded-full flex items-center justify-center text-purple-700 text-[9px] font-bold mr-2">
                        {gid.charAt(0)}
                      </div>
                      <span className="text-xs text-gray-700 font-medium">
                        {gid}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        ) : (
          !myPendingLoan && (
            <div className="bg-white rounded-2xl p-8 text-center shadow-sm border border-gray-100 border-dashed hover:border-emerald-300 transition-colors group cursor-pointer">
              <div className="mx-auto w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center mb-3">
                <FileText className="w-6 h-6 text-gray-300 group-hover:text-emerald-500 transition-colors" />
              </div>
              <p className="text-sm text-gray-500 mb-4 font-medium">
                No active loans or applications.
              </p>
              <button
                onClick={() => setShowLoanModal(true)}
                className="bg-emerald-50 text-emerald-600 hover:bg-emerald-100 px-5 py-2.5 rounded-xl text-sm font-semibold transition-colors"
              >
                Apply for Loan
              </button>
            </div>
          )
        )}
      </div>

      {/* Zone E: Recent Transactions (Bottom Feed) */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-gray-800 font-bold flex items-center text-sm uppercase tracking-wide opacity-70">
            <History className="w-4 h-4 mr-2" /> Recent Activity
          </h3>
          <button className="text-xs text-emerald-600 font-bold hover:text-emerald-700 transition-colors">
            View All
          </button>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden divide-y divide-gray-50">
          {myTransactions.slice(0, 5).map((t) => (
            <div
              key={t.id}
              className="p-4 flex items-center justify-between active:bg-gray-50 transition-colors cursor-pointer hover:bg-gray-50 group"
            >
              <div className="flex items-center space-x-4">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${
                    t.type.includes("DEPOSIT") || t.type.includes("SHARE")
                      ? "bg-emerald-100 text-emerald-600"
                      : "bg-red-100 text-red-600"
                  }`}
                >
                  {t.type.includes("DEPOSIT") || t.type.includes("SHARE") ? (
                    <ArrowDownLeft className="w-5 h-5" />
                  ) : (
                    <ArrowUpRight className="w-5 h-5" />
                  )}
                </div>
                <div>
                  <p className="text-sm font-bold text-gray-800 leading-tight">
                    {t.type.replace("_", " ")}
                  </p>
                  <p className="text-[11px] text-gray-400 mt-0.5">
                    {t.date} • {t.description}
                  </p>
                </div>
              </div>
              <span
                className={`text-sm font-bold ${
                  t.type.includes("DEPOSIT") || t.type.includes("SHARE")
                    ? "text-emerald-600"
                    : "text-red-600"
                }`}
              >
                {t.type.includes("DEPOSIT") || t.type.includes("SHARE")
                  ? "+"
                  : "-"}
                ฿{t.amount.toLocaleString()}
              </span>
            </div>
          ))}
          {myTransactions.length === 0 && (
            <div className="p-8 text-center text-gray-400 text-xs">
              No recent transactions found.
            </div>
          )}
        </div>
      </div>

      {/* Loan Application Modal */}
      <MemberLoanModal
        isOpen={showLoanModal}
        onClose={() => setShowLoanModal(false)}
        onSubmit={handleLoanSubmit}
        isSubmitting={isSubmittingLoan}
      />
    </div>
  );
};

export default MemberDashboard;
