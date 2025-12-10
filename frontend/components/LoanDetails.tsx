
import React, { useState } from 'react';
import { Loan, LoanStatus, Transaction, UserRole, CollateralDocument } from '../types';
import { 
  ArrowLeft, Banknote, Send, FileText, Receipt, Upload, Smartphone, History, 
  FileClock, X, ArrowDownLeft, QrCode, Info, Eye, Download, Calendar
} from 'lucide-react';
import DocumentViewer from './DocumentViewer';

// Helper
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

// --- OFFICER VIEW ---
interface OfficerLoanDetailProps {
    loan: Loan;
    onBack: () => void;
    onRepay: (loan: Loan) => void;
    onDisburse: (id: string) => void;
    canRepay: boolean;
    canDisburse: boolean;
}

export const OfficerLoanDetail: React.FC<OfficerLoanDetailProps> = ({ 
    loan, onBack, onRepay, onDisburse, canRepay, canDisburse 
}) => {
    const [viewerDoc, setViewerDoc] = useState<CollateralDocument | null>(null);

    return (
        <div className="space-y-6 pb-8 animate-fade-in max-w-4xl mx-auto">
            <div className="flex items-center space-x-4">
                <button 
                  onClick={onBack}
                  className="p-2.5 bg-white border border-gray-200 hover:bg-gray-50 rounded-xl transition shadow-sm"
                >
                    <ArrowLeft className="w-5 h-5 text-gray-600" />
                </button>
                <div>
                    <h2 className="text-2xl font-bold text-gray-800">Loan Details</h2>
                    <p className="text-sm text-gray-500">ID: {loan.id} • {loan.memberName}</p>
                </div>
            </div>

            <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="h-3 bg-emerald-600 w-full"></div>
                <div className="p-8">
                    <div className="flex flex-col md:flex-row justify-between items-start gap-6 mb-8 border-b border-gray-100 pb-8">
                        <div>
                            <p className="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1">Contract Number</p>
                            <p className="text-2xl font-mono font-bold text-gray-800">{loan.contractNo || 'Pending...'}</p>
                        </div>
                        <div className="text-left md:text-right">
                            <p className="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1">Current Status</p>
                            <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-bold ${getStatusColor(loan.status)}`}>
                                {loan.status}
                            </span>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-8 mb-8">
                        <div>
                            <h4 className="font-bold text-gray-800 mb-4 flex items-center gap-2">
                                <div className="w-2 h-2 rounded-full bg-emerald-500"></div> Financial Specifics
                            </h4>
                            <div className="space-y-4">
                                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                    <span className="text-gray-500 text-sm">Principal Amount</span>
                                    <span className="font-bold text-gray-800">฿{loan.principalAmount.toLocaleString()}</span>
                                </div>
                                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                    <span className="text-gray-500 text-sm">Remaining Balance</span>
                                    <span className="font-bold text-emerald-600 text-lg">฿{loan.remainingBalance.toLocaleString()}</span>
                                </div>
                                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                    <span className="text-gray-500 text-sm">Interest Rate</span>
                                    <span className="font-medium text-gray-800">{loan.interestRate}% p.a.</span>
                                </div>
                            </div>
                        </div>
                        
                        <div>
                            <h4 className="font-bold text-gray-800 mb-4 flex items-center gap-2">
                                <div className="w-2 h-2 rounded-full bg-blue-500"></div> Terms & Dates
                            </h4>
                            <div className="space-y-4">
                                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                    <span className="text-gray-500 text-sm">Loan Type</span>
                                    <span className="font-medium text-gray-800">{loan.loanType}</span>
                                </div>
                                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                    <span className="text-gray-500 text-sm">Start Date</span>
                                    <span className="font-medium text-gray-800">{loan.startDate}</span>
                                </div>
                                <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                    <span className="text-gray-500 text-sm">Duration</span>
                                    <span className="font-medium text-gray-800">{loan.termMonths} Months</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Guarantors & Security Section */}
                    <div>
                        <h4 className="font-bold text-gray-800 mb-4 flex items-center gap-2">
                            <div className="w-2 h-2 rounded-full bg-purple-500"></div> Guarantors & Security
                        </h4>
                        <div className="bg-gray-50 rounded-xl p-6 border border-gray-100">
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-6">
                                <div>
                                    <span className="text-xs font-bold text-gray-400 uppercase block mb-2">Guarantors</span>
                                    <div className="flex flex-wrap gap-2">
                                        {loan.guarantorIds.length > 0 ? (
                                            loan.guarantorIds.map(id => (
                                                <div key={id} className="flex items-center gap-2 px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm text-gray-700 font-medium shadow-sm">
                                                    <div className="w-6 h-6 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center text-xs font-bold">
                                                        {id.charAt(0)}
                                                    </div>
                                                    {id}
                                                </div>
                                            ))
                                        ) : (
                                            <span className="text-sm text-gray-500 italic">No guarantors listed</span>
                                        )}
                                    </div>
                                </div>
                                <div>
                                    <span className="text-xs font-bold text-gray-400 uppercase block mb-2">Ref Summary</span>
                                    <div className="flex items-center gap-2 px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm text-gray-700 font-medium shadow-sm w-fit">
                                        <FileText className="w-4 h-4 text-gray-400" />
                                        {loan.collateralRef || 'No text reference'}
                                    </div>
                                </div>
                            </div>

                            {/* Documents List */}
                            {loan.documents && loan.documents.length > 0 && (
                                <div className="mt-4 pt-4 border-t border-gray-200">
                                    <span className="text-xs font-bold text-gray-400 uppercase block mb-3">Attached Documents</span>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                        {loan.documents.map((doc) => (
                                            <div 
                                                key={doc.id} 
                                                onClick={() => setViewerDoc(doc)}
                                                className="flex items-center p-3 bg-white border border-gray-200 rounded-xl hover:border-emerald-400 hover:shadow-md transition cursor-pointer group"
                                            >
                                                <div className="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center shrink-0 text-gray-400 group-hover:bg-emerald-50 group-hover:text-emerald-600 overflow-hidden">
                                                    {doc.fileName.match(/\.(jpg|jpeg|png|webp)$/i) ? (
                                                        <img src={doc.url} alt="thumb" className="w-full h-full object-cover" />
                                                    ) : (
                                                        <FileText className="w-5 h-5" />
                                                    )}
                                                </div>
                                                <div className="ml-3 flex-1 min-w-0">
                                                    <p className="text-sm font-bold text-gray-800 truncate">{doc.type}</p>
                                                    <p className="text-xs text-gray-500 truncate">{doc.description}</p>
                                                </div>
                                                <Eye className="w-4 h-4 text-gray-300 group-hover:text-emerald-500" />
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    {canRepay && loan.status === LoanStatus.ACTIVE && (
                      <div className="mt-8 pt-6 border-t border-gray-100 flex justify-end">
                          <button 
                              onClick={() => onRepay(loan)}
                              className="bg-blue-600 text-white px-6 py-3 rounded-xl font-bold shadow-lg shadow-blue-200 hover:bg-blue-700 transition flex items-center gap-2"
                          >
                              <Banknote className="w-5 h-5" /> Make Repayment
                          </button>
                      </div>
                    )}

                     {canDisburse && loan.status === LoanStatus.APPROVED && (
                      <div className="mt-8 pt-6 border-t border-gray-100 flex justify-end">
                          <button 
                              onClick={() => onDisburse(loan.id)}
                              className="bg-emerald-600 text-white px-6 py-3 rounded-xl font-bold shadow-lg shadow-emerald-200 hover:bg-emerald-700 transition flex items-center gap-2"
                          >
                              <Send className="w-5 h-5" /> Disburse Funds
                          </button>
                      </div>
                    )}
                </div>
            </div>
            
            <DocumentViewer 
                isOpen={!!viewerDoc} 
                onClose={() => setViewerDoc(null)} 
                document={viewerDoc} 
            />
        </div>
    );
};

// --- MEMBER VIEW ---
interface MemberLoanDetailProps {
    loan: Loan;
    history: Transaction[];
    onBack: () => void;
    onNotifyPayment: () => void;
    onShowPaymentModal: () => void;
    onShowReceipt: (data: any) => void;
}

export const MemberLoanDetail: React.FC<MemberLoanDetailProps> = ({ 
    loan, history, onBack, onNotifyPayment, onShowPaymentModal, onShowReceipt 
}) => {
    const paidAmount = loan.principalAmount - loan.remainingBalance;
    const progress = (paidAmount / loan.principalAmount) * 100;
    const [viewerDoc, setViewerDoc] = useState<CollateralDocument | null>(null);

    // Simulated Statement Data
    const statementMonth = "October 2023";
    const broughtForward = loan.remainingBalance + 2000; // Mock BF
    const monthlyPayment = 2000;
    const monthlyInterest = 300;
    const carryForward = broughtForward - monthlyPayment + monthlyInterest;

    return (
        <div className="space-y-6 pb-8 animate-fade-in max-w-2xl mx-auto">
            <div className="flex justify-between items-center">
                <button 
                  onClick={onBack}
                  className="flex items-center text-gray-500 hover:text-gray-800 transition mb-2"
                >
                    <ArrowLeft className="w-4 h-4 mr-1" /> Back
                </button>
            </div>

            <div className="bg-gradient-to-br from-red-600 to-red-700 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden">
                <div className="absolute top-0 right-0 p-4 opacity-10">
                    <Receipt className="w-48 h-48" />
                </div>
                <div className="relative z-10">
                    <div className="flex justify-between items-start mb-6">
                        <div>
                            <p className="text-red-100 text-xs font-bold uppercase tracking-wider mb-1">Outstanding Balance</p>
                            <h2 className="text-4xl font-bold tracking-tight">฿{loan.remainingBalance.toLocaleString()}</h2>
                        </div>
                        <div className="bg-white/20 backdrop-blur-md px-3 py-1 rounded-lg">
                           <span className="text-xs font-bold text-white uppercase">{loan.status}</span>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <div className="w-full bg-black/20 h-2 rounded-full overflow-hidden">
                            <div className="bg-white h-full rounded-full transition-all duration-1000" style={{ width: `${progress}%` }}></div>
                        </div>
                        
                        {loan.status === LoanStatus.ACTIVE && (
                            <div className="flex gap-3">
                                <button 
                                    onClick={onNotifyPayment}
                                    className="flex-1 py-3 bg-white/20 backdrop-blur-sm text-white rounded-xl font-bold hover:bg-white/30 transition border border-white/30 flex items-center justify-center gap-2"
                                >
                                    <Upload className="w-5 h-5" /> Notify Transfer
                                </button>
                                <button 
                                    onClick={onShowPaymentModal}
                                    className="flex-1 py-3 bg-white text-red-600 rounded-xl font-bold hover:bg-gray-50 transition shadow-lg flex items-center justify-center gap-2"
                                >
                                    <Smartphone className="w-5 h-5" /> Pay / Settle Debt
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Monthly Statement Section (New Feature) */}
            <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="p-5 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
                    <div className="flex items-center gap-2">
                        <FileText className="w-4 h-4 text-gray-500" />
                        <h3 className="font-bold text-gray-800">Monthly Statement</h3>
                    </div>
                    <span className="text-xs font-bold bg-white border border-gray-200 px-2 py-1 rounded-lg text-gray-600">
                        {statementMonth}
                    </span>
                </div>
                <div className="p-6 relative">
                    {/* Vertical Timeline Line */}
                    <div className="absolute left-8 top-6 bottom-6 w-0.5 bg-gray-100"></div>

                    <div className="space-y-6 relative z-10">
                        {/* Brought Forward */}
                        <div className="flex items-center justify-between pl-8 relative">
                            <div className="absolute left-0 w-4 h-4 rounded-full bg-gray-200 border-2 border-white shadow-sm"></div>
                            <span className="text-sm font-medium text-gray-500">Brought Forward</span>
                            <span className="font-mono font-bold text-gray-800">฿{broughtForward.toLocaleString()}</span>
                        </div>

                        {/* Payment */}
                        <div className="flex items-center justify-between pl-8 relative">
                            <div className="absolute left-0 w-4 h-4 rounded-full bg-emerald-100 border-2 border-emerald-500 shadow-sm"></div>
                            <span className="text-sm font-medium text-gray-700">Payment Received</span>
                            <span className="font-mono font-bold text-emerald-600">-฿{monthlyPayment.toLocaleString()}</span>
                        </div>

                        {/* Interest */}
                        <div className="flex items-center justify-between pl-8 relative">
                            <div className="absolute left-0 w-4 h-4 rounded-full bg-orange-100 border-2 border-orange-500 shadow-sm"></div>
                            <span className="text-sm font-medium text-gray-700">Interest Added</span>
                            <span className="font-mono font-bold text-orange-600">+฿{monthlyInterest.toLocaleString()}</span>
                        </div>

                        {/* Divider */}
                        <div className="border-t border-dashed border-gray-200 ml-8"></div>

                        {/* Carry Forward */}
                        <div className="flex items-center justify-between pl-8 relative">
                            <div className="absolute left-0 w-4 h-4 rounded-full bg-gray-800 border-2 border-white shadow-sm"></div>
                            <span className="text-sm font-bold text-gray-800">Carry Forward</span>
                            <span className="font-mono font-bold text-xl text-gray-900 underline decoration-gray-300 underline-offset-4">฿{carryForward.toLocaleString()}</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* My Digital Assets Section */}
            {loan.documents && loan.documents.length > 0 && (
                <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
                    <div className="p-5 border-b border-gray-100 bg-gray-50/50 flex items-center gap-2">
                        <FileText className="w-4 h-4 text-gray-500" />
                        <h3 className="font-bold text-gray-800">My Digital Assets</h3>
                    </div>
                    <div className="p-5 grid gap-3">
                        {loan.documents.map(doc => (
                            <div key={doc.id} className="flex items-center justify-between p-3 rounded-xl border border-gray-100 hover:bg-gray-50 transition">
                                <div className="flex items-center gap-3">
                                    <div className="w-8 h-8 rounded-lg bg-gray-200 flex items-center justify-center text-gray-500">
                                        <FileText className="w-4 h-4" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-gray-800">{doc.type}</p>
                                        <p className="text-xs text-gray-500">{doc.description}</p>
                                    </div>
                                </div>
                                <button 
                                    onClick={() => setViewerDoc(doc)}
                                    className="text-xs font-bold text-emerald-600 hover:underline flex items-center gap-1"
                                >
                                    <Eye className="w-3 h-3" /> View
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <div className="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm flex flex-wrap gap-4 justify-between items-center text-sm">
                <div>
                    <p className="text-gray-400 text-[10px] uppercase font-bold">Contract No</p>
                    <p className="font-mono font-bold text-gray-700">{loan.contractNo || 'PENDING'}</p>
                </div>
                <div>
                    <p className="text-gray-400 text-[10px] uppercase font-bold">Loan Type</p>
                    <p className="font-bold text-gray-700">{loan.loanType}</p>
                </div>
                <div>
                    <p className="text-gray-400 text-[10px] uppercase font-bold">Interest Rate</p>
                    <p className="font-bold text-gray-700">{loan.interestRate}% p.a.</p>
                </div>
                <div>
                     <p className="text-gray-400 text-[10px] uppercase font-bold">Start Date</p>
                     <p className="font-bold text-gray-700">{loan.startDate}</p>
                </div>
            </div>

            <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="p-5 border-b border-gray-100 bg-gray-50/50 flex items-center gap-2">
                    <History className="w-4 h-4 text-gray-500" />
                    <h3 className="font-bold text-gray-800">Repayment History</h3>
                </div>
                <div className="p-5">
                    {history.length > 0 ? (
                        <div className="relative border-l-2 border-gray-100 ml-3 space-y-8 py-2">
                            {history.map((tx) => (
                                <div key={tx.id} className="relative pl-6">
                                    <div className="absolute -left-[9px] top-1 w-4 h-4 rounded-full bg-emerald-100 border-2 border-emerald-500 box-content"></div>
                                    
                                    <div className="flex justify-between items-start group">
                                        <div>
                                            <p className="text-xs font-bold text-gray-400 mb-0.5">{tx.date}</p>
                                            <p className="font-bold text-gray-800 text-sm">Installment Payment</p>
                                            <p className="text-xs text-gray-500 font-mono mt-1">Ref: {tx.receiptId}</p>
                                        </div>
                                        <div className="text-right">
                                            <p className="text-emerald-600 font-bold text-lg">-฿{tx.amount.toLocaleString()}</p>
                                            <button 
                                              onClick={() => onShowReceipt({
                                                  receiptNo: tx.receiptId,
                                                  date: new Date(tx.date).toLocaleDateString(),
                                                  receivedFrom: loan.memberName,
                                                  description: tx.description,
                                                  amount: tx.amount,
                                                  cashier: 'History'
                                              })}
                                              className="text-[10px] text-emerald-600 hover:underline flex items-center justify-end gap-1 mt-1"
                                            >
                                                <Receipt className="w-3 h-3" /> View Receipt
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="text-center py-12 text-gray-400">
                            <FileClock className="w-12 h-12 mx-auto mb-3 opacity-20" />
                            <p className="text-sm">No repayment records found.</p>
                        </div>
                    )}
                </div>
            </div>

            <DocumentViewer 
                isOpen={!!viewerDoc} 
                onClose={() => setViewerDoc(null)} 
                document={viewerDoc} 
            />
        </div>
    );
};
