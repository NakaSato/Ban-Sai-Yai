
import React, { useState } from 'react';
import { Member, Transaction } from '../../types';
import { Landmark, Calculator, Coins, DollarSign, CheckCircle, PieChart, TrendingUp, Users, ArrowRight } from 'lucide-react';

interface DividendManagerProps {
    members: Member[];
    transactions: Transaction[];
    netProfit: number;
    totalShares: number;
    canExec: boolean;
    canEdit: boolean;
}

export const DividendManager: React.FC<DividendManagerProps> = ({ members, transactions, netProfit, totalShares, canExec, canEdit }) => {
    const [dividendRate, setDividendRate] = useState<string>('4.5');
    const [avgReturnRate, setAvgReturnRate] = useState<string>('12.0');

    // Helper: Interest Paid Calculation
    const getMemberInterestPaid = (memberId: string) => {
        return transactions
        .filter(t => t.memberId === memberId && t.type === 'LOAN_REPAYMENT')
        .reduce((acc, t) => acc + (t.amount * 0.15), 0); // Mock interest portion calculation (15% of repayment)
    };

    const totalInterestReceived = members.reduce((acc, m) => acc + getMemberInterestPaid(m.id), 0);
    const totalDividendAmount = totalShares * (parseFloat(dividendRate) / 100);
    const totalReturnAmount = totalInterestReceived * (parseFloat(avgReturnRate) / 100);
    const totalDistribution = totalDividendAmount + totalReturnAmount;
    const payoutRatio = netProfit > 0 ? (totalDistribution / netProfit) * 100 : 0;

    return (
        <div className="space-y-8 animate-fade-in print:space-y-4">
            {/* Module Header */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 print:hidden">
                <div className="flex items-center space-x-4">
                    <div className="p-3 bg-gradient-to-br from-yellow-100 to-amber-100 rounded-2xl shadow-sm border border-yellow-200/50">
                        <Landmark className="w-8 h-8 text-amber-600" />
                    </div>
                    <div>
                        <h3 className="text-2xl font-bold text-gray-800 tracking-tight">Annual Profit Distribution</h3>
                        <p className="text-sm text-gray-500 font-medium">Fiscal Year 2023 • Allocation & Dividends</p>
                    </div>
                </div>
                
                {canExec && (
                    <button className="group bg-gray-900 text-white px-6 py-3 rounded-xl text-sm font-bold hover:bg-black transition-all shadow-lg hover:shadow-xl flex items-center gap-3 active:scale-95">
                        <span>Confirm Distribution</span>
                        <div className="bg-white/20 p-1 rounded-full">
                            <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
                        </div>
                    </button>
                )}
            </div>

            {/* Configuration Panel - Card Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                
                {/* 1. Rates Configuration */}
                <div className="lg:col-span-2 bg-white rounded-[2rem] shadow-sm border border-gray-100 overflow-hidden flex flex-col">
                    <div className="px-8 py-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/30">
                        <h4 className="font-bold text-gray-800 flex items-center gap-2">
                            <SettingsIcon className="w-5 h-5 text-gray-400" /> Allocation Rates
                        </h4>
                        <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Adjustable</span>
                    </div>
                    
                    <div className="p-8 grid grid-cols-1 md:grid-cols-2 gap-8 h-full items-center">
                        {/* Dividend Rate Input */}
                        <div className="relative group p-6 rounded-3xl border border-gray-100 hover:border-amber-200 hover:bg-amber-50/30 transition-all duration-300">
                            <div className="flex justify-between items-start mb-4">
                                <label className="text-xs font-bold text-gray-500 uppercase tracking-wider">Dividend on Shares</label>
                                <div className="p-2 bg-amber-100 text-amber-600 rounded-xl">
                                    <PieChart className="w-5 h-5" />
                                </div>
                            </div>
                            <div className="flex items-baseline gap-1">
                                {canEdit ? (
                                    <input 
                                        type="number" 
                                        className="text-5xl font-bold text-gray-800 w-32 outline-none bg-transparent placeholder-gray-300 focus:text-amber-600 transition-colors"
                                        value={dividendRate}
                                        onChange={(e) => setDividendRate(e.target.value)}
                                        step="0.1"
                                    />
                                ) : (
                                    <span className="text-5xl font-bold text-gray-800">{dividendRate}</span>
                                )}
                                <span className="text-2xl font-medium text-gray-400">%</span>
                            </div>
                            <p className="text-xs text-gray-400 mt-3 font-medium">
                                Base: <span className="text-gray-600">฿{totalShares.toLocaleString()}</span> (Share Capital)
                            </p>
                        </div>

                        {/* Return Rate Input */}
                        <div className="relative group p-6 rounded-3xl border border-gray-100 hover:border-blue-200 hover:bg-blue-50/30 transition-all duration-300">
                            <div className="flex justify-between items-start mb-4">
                                <label className="text-xs font-bold text-gray-500 uppercase tracking-wider">Patronage Refund</label>
                                <div className="p-2 bg-blue-100 text-blue-600 rounded-xl">
                                    <TrendingUp className="w-5 h-5" />
                                </div>
                            </div>
                            <div className="flex items-baseline gap-1">
                                {canEdit ? (
                                    <input 
                                        type="number" 
                                        className="text-5xl font-bold text-gray-800 w-32 outline-none bg-transparent placeholder-gray-300 focus:text-blue-600 transition-colors"
                                        value={avgReturnRate}
                                        onChange={(e) => setAvgReturnRate(e.target.value)}
                                        step="0.1"
                                    />
                                ) : (
                                    <span className="text-5xl font-bold text-gray-800">{avgReturnRate}</span>
                                )}
                                <span className="text-2xl font-medium text-gray-400">%</span>
                            </div>
                            <p className="text-xs text-gray-400 mt-3 font-medium">
                                Base: <span className="text-gray-600">฿{totalInterestReceived.toLocaleString()}</span> (Interest Paid)
                            </p>
                        </div>
                    </div>
                </div>

                {/* 2. Impact Summary Card */}
                <div className="bg-gradient-to-br from-emerald-900 to-teal-900 rounded-[2rem] p-8 text-white shadow-xl flex flex-col justify-between relative overflow-hidden group">
                    {/* Background decoration */}
                    <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none group-hover:bg-white/10 transition-colors duration-500"></div>
                    
                    <div className="relative z-10">
                        <div className="flex items-center gap-2 mb-6">
                            <div className="p-2 bg-white/10 rounded-lg backdrop-blur-sm">
                                <Calculator className="w-5 h-5 text-emerald-300" />
                            </div>
                            <span className="text-sm font-bold text-emerald-100 uppercase tracking-wider">Impact Analysis</span>
                        </div>

                        <div className="space-y-6">
                            <div>
                                <p className="text-emerald-200/70 text-xs font-bold uppercase tracking-wider mb-1">Total Net Profit</p>
                                <p className="text-2xl font-bold text-white tracking-tight">฿{netProfit.toLocaleString()}</p>
                            </div>
                            
                            <div className="pt-6 border-t border-white/10">
                                <p className="text-emerald-200/70 text-xs font-bold uppercase tracking-wider mb-2">Total Distribution</p>
                                <p className="text-4xl font-bold text-white tracking-tight mb-2">
                                    ฿{totalDistribution.toLocaleString(undefined, {maximumFractionDigits: 0})}
                                </p>
                                <div className="flex items-center gap-3">
                                    <div className="flex-1 h-2 bg-black/30 rounded-full overflow-hidden backdrop-blur-sm">
                                        <div 
                                            className={`h-full rounded-full transition-all duration-500 ${payoutRatio > 100 ? 'bg-red-500' : 'bg-emerald-400'}`} 
                                            style={{ width: `${Math.min(payoutRatio, 100)}%` }}
                                        />
                                    </div>
                                    <span className={`text-xs font-bold ${payoutRatio > 100 ? 'text-red-300' : 'text-emerald-300'}`}>
                                        {payoutRatio.toFixed(1)}%
                                    </span>
                                </div>
                                {payoutRatio > 100 && (
                                    <p className="text-xs text-red-300 mt-2 font-bold bg-red-500/20 px-2 py-1 rounded-lg w-fit">
                                        Warning: Exceeds Net Profit
                                    </p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Calculation Preview Table */}
            <div className="bg-white rounded-[2rem] shadow-sm border border-gray-200 overflow-hidden print:shadow-none print:border-none print:rounded-none">
                <div className="p-8 border-b border-gray-100 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-gray-50/30 print:bg-white print:px-0">
                    <div>
                        <h3 className="font-bold text-gray-800 text-lg flex items-center gap-2 print:text-black">
                            <Users className="w-5 h-5 text-gray-400" /> Member Payout Preview
                        </h3>
                        <p className="text-sm text-gray-500 mt-1">Detailed breakdown by member based on current rates.</p>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-gray-500 font-medium">
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-amber-400"></div> Dividend
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-blue-400"></div> Refund
                        </div>
                    </div>
                </div>
                
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-gray-100 text-xs uppercase text-gray-500 font-bold border-b border-gray-200 print:bg-white print:text-black print:border-black print:border-b-2">
                            <tr>
                                <th className="p-5 pl-8 border-r border-gray-100 print:p-2 print:border-black w-[25%]">Member Name</th>
                                <th className="p-5 text-right print:p-2 text-gray-400">Shares Base</th>
                                <th className="p-5 text-right text-amber-700 bg-amber-50/30 border-l border-gray-100 print:bg-white print:text-black print:border-black print:p-2 w-[15%]">Dividend</th>
                                <th className="p-5 text-right text-gray-400 border-l border-gray-100 print:border-black print:p-2">Interest Base</th>
                                <th className="p-5 text-right text-blue-700 bg-blue-50/30 border-l border-gray-100 print:bg-white print:text-black print:border-black print:p-2 w-[15%]">Refund</th>
                                <th className="p-5 pr-8 text-right font-bold text-gray-800 border-l border-gray-200 bg-gray-50 print:bg-white print:text-black print:border-black print:p-2 w-[20%]">Total Payout</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 text-sm print:divide-gray-300">
                            {members.map((m, idx) => {
                                const interestPaid = getMemberInterestPaid(m.id);
                                const divAmt = m.shareBalance * (parseFloat(dividendRate)/100);
                                const retAmt = interestPaid * (parseFloat(avgReturnRate)/100); 
                                return (
                                    <tr key={m.id} className="group hover:bg-gray-50 transition-colors print:hover:bg-transparent">
                                        <td className="p-5 pl-8 border-r border-gray-100 print:text-black print:border-black print:p-2">
                                            <div className="font-bold text-gray-800">{m.fullName}</div>
                                            <div className="text-xs text-gray-400 font-mono mt-0.5">{m.id}</div>
                                        </td>
                                        <td className="p-5 text-right text-gray-400 font-mono print:text-black print:p-2 text-xs">฿{m.shareBalance.toLocaleString()}</td>
                                        <td className="p-5 text-right font-bold text-amber-600 bg-amber-50/30 group-hover:bg-amber-100/30 border-l border-gray-100 transition-colors print:bg-white print:text-black print:border-black print:p-2">
                                            ฿{divAmt.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                                        </td>
                                        <td className="p-5 text-right text-gray-400 font-mono border-l border-gray-100 print:text-black print:border-black print:p-2 text-xs">฿{interestPaid.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}</td>
                                        <td className="p-5 text-right font-bold text-blue-600 bg-blue-50/30 group-hover:bg-blue-100/30 border-l border-gray-100 transition-colors print:bg-white print:text-black print:border-black print:p-2">
                                            ฿{retAmt.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                                        </td>
                                        <td className="p-5 pr-8 text-right font-bold text-emerald-700 bg-emerald-50/10 border-l border-gray-200 print:bg-white print:text-black print:border-black print:p-2">
                                            ฿{(divAmt + retAmt).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                        <tfoot className="bg-gray-50 border-t-2 border-gray-200 print:bg-white print:border-black print:border-t-2">
                            <tr>
                                <td className="p-5 pl-8 font-bold text-gray-600 uppercase tracking-wide text-xs">Grand Total</td>
                                <td className="p-5 text-right text-gray-400 font-mono text-xs">฿{totalShares.toLocaleString()}</td>
                                <td className="p-5 text-right font-bold text-amber-700">฿{totalDividendAmount.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                                <td className="p-5 text-right text-gray-400 font-mono text-xs">฿{totalInterestReceived.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                                <td className="p-5 text-right font-bold text-blue-700">฿{totalReturnAmount.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                                <td className="p-5 pr-8 text-right font-bold text-emerald-700 text-lg">฿{totalDistribution.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    );
};

const SettingsIcon = ({ className }: { className?: string }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.1a2 2 0 0 1-1-1.74v-.47a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
);
