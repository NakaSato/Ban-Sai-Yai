
import React, { useState, useEffect } from 'react';
import { Member, Loan, Transaction } from '../types';
import { Search, X, User, CreditCard, Banknote, Calculator, AlertTriangle, CheckCircle, Receipt, ArrowRight } from 'lucide-react';

interface PaymentTerminalProps {
    isOpen: boolean;
    onClose: () => void;
    members: Member[];
    loans: Loan[];
    onProcessPayment: (data: any) => void;
}

export const PaymentTerminal: React.FC<PaymentTerminalProps> = ({ isOpen, onClose, members, loans, onProcessPayment }) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedMember, setSelectedMember] = useState<Member | null>(null);
    const [activeTab, setActiveTab] = useState<'SHARES' | 'LOAN'>('SHARES');
    
    // Transaction States
    const [shareAmount, setShareAmount] = useState('');
    const [loanId, setLoanId] = useState('');
    const [repaymentAmount, setRepaymentAmount] = useState('');
    
    // Derived Data
    const memberLoans = selectedMember ? loans.filter(l => l.memberId === selectedMember.id && l.status === 'ACTIVE') : [];
    const selectedLoan = memberLoans.find(l => l.id === loanId);

    // Auto-select first loan if available
    useEffect(() => {
        if (memberLoans.length > 0 && !loanId) {
            setLoanId(memberLoans[0].id);
        }
    }, [memberLoans, loanId]);

    // Reset when modal closes or member changes
    useEffect(() => {
        if (!isOpen) {
            setSearchQuery('');
            setSelectedMember(null);
            setShareAmount('');
            setRepaymentAmount('');
        }
    }, [isOpen]);

    const handleSearch = () => {
        const found = members.find(m => 
            m.id.toLowerCase() === searchQuery.toLowerCase() || 
            m.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            m.idCardNumber?.includes(searchQuery)
        );
        if (found) {
            setSelectedMember(found);
            setSearchQuery('');
        } else {
            alert('Member not found');
        }
    };

    const calculateLoanBreakdown = () => {
        if (!selectedLoan || !repaymentAmount) return { interest: 0, principal: 0, balanceAfter: 0 };
        
        const amount = parseFloat(repaymentAmount);
        // Mock Interest Logic: 1 month of interest
        const monthlyInterest = (selectedLoan.remainingBalance * (selectedLoan.interestRate / 100)) / 12;
        
        const interestPart = Math.min(amount, monthlyInterest);
        const principalPart = amount - interestPart;
        const balanceAfter = selectedLoan.remainingBalance - principalPart;

        return { 
            interest: interestPart, 
            principal: Math.max(0, principalPart), 
            balanceAfter: Math.max(0, balanceAfter) 
        };
    };

    const breakdown = calculateLoanBreakdown();

    const handleSubmit = () => {
        if (!selectedMember) return;

        if (activeTab === 'SHARES') {
            onProcessPayment({
                type: 'SHARE_PURCHASE',
                memberId: selectedMember.id,
                amount: parseFloat(shareAmount),
                description: 'Share Purchase via Terminal'
            });
        } else {
            if (!selectedLoan) return;
            onProcessPayment({
                type: 'LOAN_REPAYMENT',
                memberId: selectedMember.id,
                loanId: selectedLoan.id,
                amount: parseFloat(repaymentAmount),
                description: `Loan Repayment (${selectedLoan.id})`,
                breakdown
            });
        }
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-gray-900/80 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fade-in">
            <div className="bg-white w-full max-w-5xl h-[600px] rounded-3xl shadow-2xl overflow-hidden flex flex-col md:flex-row">
                
                {/* LEFT PANEL: Member Context */}
                <div className="w-full md:w-2/5 bg-slate-900 text-white p-8 flex flex-col relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-12 opacity-5 pointer-events-none">
                        <User className="w-64 h-64" />
                    </div>

                    <div className="relative z-10 flex-1">
                        <h2 className="text-xl font-bold text-emerald-400 mb-6 flex items-center gap-2">
                            <CreditCard className="w-5 h-5" /> Member Identification
                        </h2>

                        {/* Search Box */}
                        <div className="relative mb-8">
                            <input 
                                type="text" 
                                placeholder="Scan ID or Type Name..." 
                                className="w-full bg-slate-800 border border-slate-700 rounded-xl py-3 pl-10 pr-4 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 transition"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                                autoFocus={!selectedMember}
                            />
                            <Search className="absolute left-3 top-3.5 w-4 h-4 text-slate-500" />
                        </div>

                        {selectedMember ? (
                            <div className="space-y-6 animate-in fade-in slide-in-from-left-4">
                                <div className="flex items-center gap-4">
                                    <div className="w-16 h-16 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-full flex items-center justify-center text-2xl font-bold shadow-lg">
                                        {selectedMember.fullName.charAt(0)}
                                    </div>
                                    <div>
                                        <h3 className="text-xl font-bold leading-tight">{selectedMember.fullName}</h3>
                                        <p className="text-slate-400 font-mono text-sm">{selectedMember.id}</p>
                                    </div>
                                </div>

                                <div className="bg-slate-800/50 rounded-2xl p-4 border border-slate-700/50 space-y-3">
                                    <div className="flex justify-between items-center">
                                        <span className="text-slate-400 text-sm">Accumulated Shares</span>
                                        <span className="text-emerald-400 font-bold font-mono">฿{selectedMember.shareBalance.toLocaleString()}</span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-slate-400 text-sm">Savings Account</span>
                                        <span className="text-blue-400 font-bold font-mono">฿{selectedMember.savingsBalance.toLocaleString()}</span>
                                    </div>
                                </div>

                                {memberLoans.length > 0 ? (
                                    <div className="bg-red-900/20 rounded-2xl p-4 border border-red-900/30">
                                        <div className="flex justify-between items-center mb-2">
                                            <span className="text-red-400 text-xs font-bold uppercase tracking-wider">Active Debt</span>
                                            <span className="bg-red-500 text-white text-[10px] px-2 py-0.5 rounded-full font-bold">{memberLoans.length} Loan(s)</span>
                                        </div>
                                        <div className="text-2xl font-bold text-red-100 font-mono">
                                            ฿{memberLoans.reduce((sum, l) => sum + l.remainingBalance, 0).toLocaleString()}
                                        </div>
                                    </div>
                                ) : (
                                    <div className="flex items-center gap-2 text-emerald-500 bg-emerald-900/20 p-3 rounded-xl border border-emerald-900/30">
                                        <CheckCircle className="w-5 h-5" />
                                        <span className="text-sm font-medium">No Outstanding Debt</span>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="text-center text-slate-500 mt-12">
                                <Search className="w-12 h-12 mx-auto mb-3 opacity-20" />
                                <p>Search for a member to begin transaction.</p>
                            </div>
                        )}
                    </div>
                    
                    <button onClick={onClose} className="text-slate-500 hover:text-white flex items-center gap-2 text-sm transition mt-auto">
                        <ArrowRight className="w-4 h-4 rotate-180" /> Cancel Transaction
                    </button>
                </div>

                {/* RIGHT PANEL: Transaction Input */}
                <div className="w-full md:w-3/5 bg-gray-50 p-8 flex flex-col">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
                            <Banknote className="w-6 h-6 text-gray-600" /> Transaction Entry
                        </h2>
                        <button onClick={onClose} className="p-2 hover:bg-gray-200 rounded-full transition text-gray-400">
                            <X className="w-5 h-5" />
                        </button>
                    </div>

                    <div className="flex p-1 bg-gray-200 rounded-xl mb-6">
                        <button 
                            onClick={() => setActiveTab('SHARES')}
                            className={`flex-1 py-3 rounded-lg text-sm font-bold transition flex items-center justify-center gap-2 ${activeTab === 'SHARES' ? 'bg-white shadow-sm text-emerald-600' : 'text-gray-500 hover:text-gray-700'}`}
                        >
                            <User className="w-4 h-4" /> Share / Savings
                        </button>
                        <button 
                            onClick={() => setActiveTab('LOAN')}
                            disabled={!selectedMember || memberLoans.length === 0}
                            className={`flex-1 py-3 rounded-lg text-sm font-bold transition flex items-center justify-center gap-2 ${activeTab === 'LOAN' ? 'bg-white shadow-sm text-red-600' : 'text-gray-500 hover:text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed'}`}
                        >
                            <Receipt className="w-4 h-4" /> Debt Repayment
                        </button>
                    </div>

                    <div className="flex-1 bg-white rounded-2xl border border-gray-200 p-6 shadow-sm relative">
                        {activeTab === 'SHARES' && (
                            <div className="space-y-6 animate-in fade-in slide-in-from-right-4">
                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Transaction Type</label>
                                    <select className="w-full border border-gray-300 rounded-xl px-4 py-3 bg-white outline-none focus:ring-2 focus:ring-emerald-500">
                                        <option value="SHARE">Buy Shares (ฝากหุ้น)</option>
                                        <option value="DEPOSIT">Deposit Savings (ฝากออมทรัพย์)</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Amount Received</label>
                                    <div className="relative">
                                        <span className="absolute left-4 top-1/2 -translate-y-1/2 text-xl font-bold text-gray-400">฿</span>
                                        <input 
                                            type="number" 
                                            className="w-full border border-gray-300 rounded-xl pl-10 pr-4 py-4 text-3xl font-bold text-gray-800 outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition"
                                            placeholder="0.00"
                                            value={shareAmount}
                                            onChange={(e) => setShareAmount(e.target.value)}
                                            disabled={!selectedMember}
                                        />
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'LOAN' && (
                            <div className="space-y-6 animate-in fade-in slide-in-from-right-4">
                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Select Loan Contract</label>
                                    <select 
                                        className="w-full border border-gray-300 rounded-xl px-4 py-3 bg-white outline-none focus:ring-2 focus:ring-red-500"
                                        value={loanId}
                                        onChange={(e) => setLoanId(e.target.value)}
                                    >
                                        {memberLoans.map(l => (
                                            <option key={l.id} value={l.id}>
                                                {l.contractNo || l.id} - Bal: ฿{l.remainingBalance.toLocaleString()}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Total Cash Payment</label>
                                    <div className="relative">
                                        <span className="absolute left-4 top-1/2 -translate-y-1/2 text-xl font-bold text-gray-400">฿</span>
                                        <input 
                                            type="number" 
                                            className="w-full border border-gray-300 rounded-xl pl-10 pr-4 py-4 text-3xl font-bold text-gray-800 outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 transition"
                                            placeholder="0.00"
                                            value={repaymentAmount}
                                            onChange={(e) => setRepaymentAmount(e.target.value)}
                                        />
                                    </div>
                                </div>

                                {/* Dynamic Calculation Feedback */}
                                {parseFloat(repaymentAmount) > 0 && (
                                    <div className="bg-orange-50 rounded-xl p-4 border border-orange-100 space-y-2">
                                        <div className="flex justify-between text-sm">
                                            <span className="text-orange-800 flex items-center gap-1"><AlertTriangle className="w-3 h-3" /> Interest Portion</span>
                                            <span className="font-bold text-orange-700">฿{breakdown.interest.toLocaleString(undefined, {minimumFractionDigits: 2})}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-emerald-800 flex items-center gap-1"><CheckCircle className="w-3 h-3" /> Principal Portion</span>
                                            <span className="font-bold text-emerald-700">฿{breakdown.principal.toLocaleString(undefined, {minimumFractionDigits: 2})}</span>
                                        </div>
                                        <div className="border-t border-orange-200 pt-2 mt-1 flex justify-between text-xs font-medium text-gray-500">
                                            <span>Balance After Payment:</span>
                                            <span>฿{breakdown.balanceAfter.toLocaleString(undefined, {minimumFractionDigits: 2})}</span>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    <button 
                        onClick={handleSubmit}
                        disabled={!selectedMember || (activeTab === 'SHARES' && !shareAmount) || (activeTab === 'LOAN' && !repaymentAmount)}
                        className="w-full mt-6 py-4 bg-gray-900 text-white rounded-xl font-bold text-lg hover:bg-black transition shadow-lg shadow-gray-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                    >
                        <Receipt className="w-5 h-5" /> Confirm Transaction
                    </button>
                </div>
            </div>
        </div>
    );
};
