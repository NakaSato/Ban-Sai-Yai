
import React from 'react';
import { Loan, LoanStatus, UserRole, PaymentNotification } from '../types';
import { 
  Eye, Banknote, Send, CheckCircle, X, Lock, Image, ChevronRight, FileText 
} from 'lucide-react';

// --- Helpers ---
const getStatusColor = (status: LoanStatus) => {
  switch(status) {
    case LoanStatus.ACTIVE: return 'bg-emerald-100 text-emerald-800 border border-emerald-200';
    case LoanStatus.APPROVED: return 'bg-blue-100 text-blue-800 border border-blue-200';
    case LoanStatus.PENDING: return 'bg-yellow-100 text-yellow-800 border border-yellow-200';
    case LoanStatus.REJECTED: return 'bg-red-100 text-red-800 border border-red-200';
    case LoanStatus.DEFAULTED: return 'bg-gray-800 text-white border border-gray-600';
    case LoanStatus.PAID: return 'bg-gray-100 text-gray-800 border border-gray-200';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// --- Active Loans Table ---
interface ActiveLoansTableProps {
  loans: Loan[];
  onView: (loan: Loan) => void;
  onRepay: (loan: Loan) => void;
  onDisburse: (id: string) => void;
  canRepay: boolean;
  canDisburse: boolean;
}

export const ActiveLoansTable: React.FC<ActiveLoansTableProps> = ({ 
  loans, onView, onRepay, onDisburse, canRepay, canDisburse 
}) => {
  return (
    <div className="animate-fade-in">
        {/* Mobile Card View */}
        <div className="md:hidden p-4 space-y-4 bg-gray-50 min-h-[300px]">
            {loans.map((loan) => {
                 const progress = loan.principalAmount > 0 
                      ? Math.min(100, Math.max(0, ((loan.principalAmount - loan.remainingBalance) / loan.principalAmount) * 100)) 
                      : 0;
                 return (
                    <div key={loan.id} className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100">
                        <div className="flex justify-between items-start mb-4">
                            <div>
                                <div className="flex items-center gap-2 mb-1">
                                    <span className="font-mono text-xs text-gray-400 bg-gray-50 px-1.5 py-0.5 rounded">{loan.id}</span>
                                    <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase border ${getStatusColor(loan.status)}`}>
                                        {loan.status}
                                    </span>
                                </div>
                                <h4 className="font-bold text-gray-800 text-lg">{loan.memberName}</h4>
                                <p className="text-xs text-gray-500">{loan.startDate} • {loan.loanType}</p>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4 mb-4 p-3 bg-gray-50 rounded-xl">
                            <div>
                                <p className="text-[10px] text-gray-400 uppercase font-bold">Principal</p>
                                <p className="font-medium text-gray-700">฿{loan.principalAmount.toLocaleString()}</p>
                            </div>
                            <div className="text-right">
                                <p className="text-[10px] text-gray-400 uppercase font-bold">Balance</p>
                                <p className="font-bold text-emerald-600 text-lg">฿{loan.remainingBalance.toLocaleString()}</p>
                            </div>
                        </div>
                        
                        <div className="mb-5">
                            <div className="flex justify-between text-xs mb-1.5">
                                <span className="text-gray-500 font-medium">Repayment Progress</span>
                                <span className="font-bold text-gray-700">{progress.toFixed(0)}%</span>
                            </div>
                            <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden border border-gray-100">
                                <div className="bg-emerald-500 h-2 rounded-full transition-all duration-500" style={{ width: `${progress}%` }}></div>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                             <button 
                                  onClick={() => onView(loan)}
                                  className="py-2.5 text-gray-600 bg-white border border-gray-200 hover:bg-gray-50 rounded-xl text-sm font-bold transition flex items-center justify-center gap-2"
                              >
                                  <Eye className="w-4 h-4" /> Details
                              </button>
                              {canRepay && loan.status === LoanStatus.ACTIVE && (
                                  <button 
                                      onClick={() => onRepay(loan)}
                                      className="py-2.5 bg-blue-600 text-white rounded-xl text-sm font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-200/50 flex items-center justify-center gap-2"
                                  >
                                      <Banknote className="w-4 h-4" /> Repay
                                  </button>
                              )}
                               {canDisburse && loan.status === LoanStatus.APPROVED && (
                                  <button 
                                      onClick={() => onDisburse(loan.id)}
                                      className="py-2.5 bg-emerald-600 text-white rounded-xl text-sm font-bold hover:bg-emerald-700 transition shadow-lg shadow-emerald-200/50 flex items-center justify-center gap-2"
                                  >
                                      <Send className="w-4 h-4" /> Payout
                                  </button>
                              )}
                        </div>
                    </div>
                 );
            })}
             {loans.length === 0 && (
                <div className="flex flex-col items-center justify-center py-16 text-center text-gray-400">
                    <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                        <FileText className="w-8 h-8 text-gray-300" />
                    </div>
                    <p className="font-medium text-gray-500">No active loans found.</p>
                    <p className="text-xs text-gray-400 mt-1 max-w-[200px]">New loans will appear here once approved and disbursed.</p>
                </div>
            )}
        </div>

        {/* Desktop Table View */}
        <div className="hidden md:block overflow-x-auto min-h-[400px]">
            <table className="w-full text-left">
            <thead className="bg-gray-50/50 border-b border-gray-200">
                <tr>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider">ID</th>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider">Borrower</th>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider">Start Date</th>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider text-right">Principal</th>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider text-right">Balance</th>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider">Status</th>
                <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider text-center">Actions</th>
                </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-sm">
                {loans.map((loan) => {
                  const progress = loan.principalAmount > 0 
                      ? Math.min(100, Math.max(0, ((loan.principalAmount - loan.remainingBalance) / loan.principalAmount) * 100)) 
                      : 0;

                  return (
                  <tr key={loan.id} className="hover:bg-gray-50 transition">
                      <td className="p-4 font-mono text-gray-500">{loan.id}</td>
                      <td className="p-4 font-bold text-gray-700">{loan.memberName}</td>
                      <td className="p-4 text-gray-500">{loan.startDate}</td>
                      <td className="p-4 text-gray-600 text-right">฿{loan.principalAmount.toLocaleString()}</td>
                      <td className="p-4 text-right">
                          <div className="text-emerald-600 font-bold">฿{loan.remainingBalance.toLocaleString()}</div>
                          {/* Progress Bar Feature */}
                          <div className="w-32 ml-auto bg-gray-100 rounded-full h-1.5 mt-2 overflow-hidden border border-gray-100">
                              <div className="bg-emerald-500 h-1.5 rounded-full transition-all duration-500" style={{ width: `${progress}%` }}></div>
                          </div>
                          <div className="text-[10px] text-gray-400 mt-1 font-medium">{progress.toFixed(0)}% Paid</div>
                      </td>
                      <td className="p-4">
                      <span className={`inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold border ${getStatusColor(loan.status)}`}>
                          {loan.status}
                      </span>
                      </td>
                      <td className="p-4 text-center">
                          <div className="flex items-center justify-center space-x-2">
                              <button 
                                  onClick={() => onView(loan)}
                                  className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition"
                                  title="View Details"
                              >
                                  <Eye className="w-4 h-4" />
                              </button>
                              {canRepay && loan.status === LoanStatus.ACTIVE && (
                                  <button 
                                      onClick={() => onRepay(loan)}
                                      className="bg-blue-50 text-blue-600 hover:bg-blue-100 px-3 py-1.5 rounded-lg text-xs font-bold transition border border-blue-100"
                                  >
                                      Repay
                                  </button>
                              )}
                               {canDisburse && loan.status === LoanStatus.APPROVED && (
                                  <button 
                                      onClick={() => onDisburse(loan.id)}
                                      className="bg-emerald-50 text-emerald-600 hover:bg-emerald-100 px-3 py-1.5 rounded-lg text-xs font-bold transition border border-emerald-100"
                                  >
                                      Payout
                                  </button>
                              )}
                          </div>
                      </td>
                  </tr>
                  );
                })}
                 {loans.length === 0 && (
                    <tr>
                        <td colSpan={7} className="p-16 text-center text-gray-500">
                             <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4 mx-auto">
                                <FileText className="w-8 h-8 text-gray-300" />
                            </div>
                            <p className="font-medium text-gray-600">No active loans found matching your criteria.</p>
                        </td>
                    </tr>
                )}
            </tbody>
            </table>
        </div>
    </div>
  );
};

// --- Loan Requests Table ---
interface LoanRequestsTableProps {
  loans: Loan[];
  onApprove: (id: string) => void;
  onReject: (id: string) => void;
  onDisburse: (id: string) => void;
  canApprove: boolean;
  canDisburse: boolean;
}

export const LoanRequestsTable: React.FC<LoanRequestsTableProps> = ({
  loans, onApprove, onReject, onDisburse, canApprove, canDisburse
}) => {
  return (
    <div className="p-4 md:p-6 space-y-4 animate-fade-in bg-gray-50 min-h-[400px]">
        {loans.map(loan => (
            <div key={loan.id} className="bg-white p-5 md:p-6 rounded-3xl shadow-sm border border-gray-200 flex flex-col md:flex-row justify-between items-start md:items-center gap-6 hover:shadow-md transition">
                <div className="flex-1 w-full">
                    <div className="flex items-center space-x-3 mb-2">
                        {loan.status === LoanStatus.PENDING ? (
                            <span className="bg-yellow-100 text-yellow-800 text-xs px-2.5 py-1 rounded-lg font-bold border border-yellow-200 whitespace-nowrap">Pending Approval</span>
                        ) : (
                            <span className="bg-blue-100 text-blue-800 text-xs px-2.5 py-1 rounded-lg font-bold border border-blue-200 whitespace-nowrap">Approved (Pending Payout)</span>
                        )}
                        <h3 className="font-bold text-gray-800 text-lg truncate">{loan.memberName}</h3>
                    </div>
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm text-gray-600 mt-4">
                        <div>
                            <p className="text-[10px] text-gray-400 uppercase font-bold">Amount</p>
                            <p className="font-bold text-emerald-600">฿{loan.principalAmount.toLocaleString()}</p>
                        </div>
                        <div>
                            <p className="text-[10px] text-gray-400 uppercase font-bold">Type</p>
                            <p className="font-medium">{loan.loanType}</p>
                        </div>
                          <div>
                            <p className="text-[10px] text-gray-400 uppercase font-bold">Term</p>
                            <p className="font-medium">{loan.termMonths} Mo.</p>
                        </div>
                          <div>
                            <p className="text-[10px] text-gray-400 uppercase font-bold">Guarantors</p>
                            <p className="font-medium">{loan.guarantorIds.length}</p>
                        </div>
                    </div>
                </div>
                
                <div className="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
                  {loan.status === LoanStatus.PENDING ? (
                      canApprove ? (
                          <>
                              <button 
                                  onClick={() => onReject(loan.id)}
                                  className="px-5 py-2.5 border border-red-200 text-red-600 rounded-xl hover:bg-red-50 transition font-bold text-sm flex items-center justify-center gap-2 w-full sm:w-auto"
                              >
                                  <X className="w-4 h-4" /> Reject
                              </button>
                              <button 
                                  onClick={() => onApprove(loan.id)}
                                  className="px-5 py-2.5 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 transition shadow-lg shadow-emerald-200/50 font-bold text-sm flex items-center justify-center gap-2 w-full sm:w-auto"
                              >
                                  <CheckCircle className="w-4 h-4" /> Approve
                              </button>
                          </>
                      ) : (
                          <div className="flex items-center justify-center text-orange-600 bg-orange-50 px-4 py-3 rounded-xl border border-orange-100 w-full sm:w-auto">
                              <Lock className="w-4 h-4 mr-2" />
                              <span className="text-sm font-bold">Approval Required</span>
                          </div>
                      )
                  ) : (
                      canDisburse ? (
                          <button 
                              onClick={() => onDisburse(loan.id)}
                              className="px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition shadow-lg shadow-blue-200/50 font-bold text-sm flex items-center justify-center gap-2 w-full sm:w-auto"
                          >
                              <Send className="w-4 h-4" />
                              <span>Disburse Funds</span>
                          </button>
                      ) : (
                            <div className="flex items-center justify-center text-blue-600 bg-blue-50 px-4 py-3 rounded-xl border border-blue-100 w-full sm:w-auto">
                              <Lock className="w-4 h-4 mr-2" />
                              <span className="text-sm font-medium">Awaiting Payout</span>
                          </div>
                      )
                  )}
                </div>
            </div>
        ))}
        {loans.length === 0 && (
             <div className="p-12 text-center text-gray-500 flex flex-col items-center">
                <CheckCircle className="w-12 h-12 text-gray-200 mb-3" />
                <p>All clear. No pending approvals.</p>
            </div>
        )}
    </div>
  );
};

// --- Online Verification Table ---
interface OnlineVerificationTableProps {
  notifications: PaymentNotification[];
  onVerify: (notif: PaymentNotification) => void;
}

export const OnlineVerificationTable: React.FC<OnlineVerificationTableProps> = ({
  notifications, onVerify
}) => {
  return (
    <div className="animate-fade-in">
        {/* Mobile View */}
         <div className="md:hidden space-y-4 p-4 bg-gray-50 min-h-[300px]">
          {notifications.map(notif => (
            <div key={notif.id} className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100">
                <div className="flex justify-between items-start mb-4">
                     <div className="flex items-center gap-3">
                         <div className="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600 font-bold text-sm">
                             {notif.memberName.charAt(0)}
                         </div>
                         <div>
                             <p className="font-bold text-sm text-gray-800">{notif.memberName}</p>
                             <p className="text-[10px] text-gray-400">{notif.date} • {new Date(notif.timestamp).toLocaleTimeString()}</p>
                         </div>
                     </div>
                     <div className="text-right">
                         <p className="text-[10px] text-gray-400 uppercase font-bold">Amount</p>
                         <p className="font-bold text-emerald-600 text-lg">฿{notif.amount.toLocaleString()}</p>
                     </div>
                </div>
                <div className="flex gap-3">
                     <button className="flex-1 py-2.5 text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-xl text-xs font-bold flex items-center justify-center gap-2 border border-blue-100">
                         <Image className="w-4 h-4" /> View Slip
                     </button>
                     <button onClick={() => onVerify(notif)} className="flex-[2] py-2.5 bg-emerald-600 text-white rounded-xl text-xs font-bold hover:bg-emerald-700 transition shadow-lg shadow-emerald-200/50">
                         Verify & Approve
                     </button>
                </div>
            </div>
          ))}
           {notifications.length === 0 && (
                <div className="text-center py-16 text-gray-400">
                     <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4 mx-auto">
                        <CheckCircle className="w-8 h-8 text-gray-300" />
                    </div>
                    <p>No verification requests.</p>
                </div>
            )}
        </div>

        {/* Desktop View */}
        <div className="hidden md:block overflow-x-auto min-h-[400px]">
            <table className="w-full text-left">
                <thead className="bg-gray-50/50 border-b border-gray-200">
                    <tr>
                        <th className="p-4 text-xs font-bold uppercase text-gray-500">Date/Time</th>
                        <th className="p-4 text-xs font-bold uppercase text-gray-500">Member</th>
                        <th className="p-4 text-xs font-bold uppercase text-gray-500 text-right">Amount</th>
                        <th className="p-4 text-xs font-bold uppercase text-gray-500 text-center">Slip</th>
                        <th className="p-4 text-xs font-bold uppercase text-gray-500 text-center">Action</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 text-sm">
                    {notifications.map((notif) => (
                        <tr key={notif.id} className="hover:bg-gray-50 transition">
                            <td className="p-4">
                                <p className="font-bold text-gray-700">{notif.date}</p>
                                <p className="text-xs text-gray-400">{new Date(notif.timestamp).toLocaleTimeString()}</p>
                            </td>
                            <td className="p-4">
                                <p className="font-bold text-gray-800">{notif.memberName}</p>
                                <p className="text-xs text-gray-500">{notif.memberId}</p>
                            </td>
                            <td className="p-4 text-right font-bold text-emerald-600">
                                ฿{notif.amount.toLocaleString()}
                            </td>
                            <td className="p-4 text-center">
                                <button className="text-blue-600 text-xs font-bold underline flex items-center justify-center gap-1 mx-auto hover:text-blue-800">
                                    <Image className="w-3 h-3" /> View Slip
                                </button>
                            </td>
                            <td className="p-4 text-center">
                                <button 
                                    onClick={() => onVerify(notif)}
                                    className="bg-emerald-600 text-white px-4 py-1.5 rounded-lg text-xs font-bold hover:bg-emerald-700 transition shadow-sm"
                                >
                                    Verify & Approve
                                </button>
                            </td>
                        </tr>
                    ))}
                    {notifications.length === 0 && (
                        <tr>
                            <td colSpan={5} className="p-16 text-center text-gray-400">
                                <CheckCircle className="w-12 h-12 mx-auto mb-3 text-gray-200" />
                                <p>All online payments verified.</p>
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    </div>
  );
};
