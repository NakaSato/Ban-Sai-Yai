
import React from 'react';
import { Account } from '../types';
import { 
  Landmark, Wallet, PieChart, TrendingUp, CheckCircle, AlertCircle, FileBarChart, ArrowRight, TrendingDown 
} from 'lucide-react';
import { 
  PieChart as RePieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend, BarChart, Bar, XAxis, YAxis, CartesianGrid 
} from 'recharts';

interface FinancialStatementsProps {
    assets: number;
    liabilities: number;
    equity: number;
    netProfit: number;
    cashAssets: Account[];
    otherLiabilities: Account[];
    otherEquity: Account[];
    revenueAccounts: Account[];
    expenseAccounts: Account[];
    assetCompositionData: any[];
    performanceData: any[];
}

export const FinancialStatements: React.FC<FinancialStatementsProps> = ({
    assets, liabilities, equity, netProfit, cashAssets, otherLiabilities, otherEquity, revenueAccounts, expenseAccounts, assetCompositionData, performanceData
}) => {
    const totalLiabilitiesEquity = liabilities + equity;
    const isBalanced = Math.abs(assets - totalLiabilitiesEquity) < 1;

    // Helper sums
    const totalCashAssets = cashAssets.reduce((sum, a) => sum + a.balance, 0);
    const totalLoansRemaining = assets - totalCashAssets;
    const revenue = revenueAccounts.reduce((acc, a) => acc + a.balance, 0);
    const expenses = expenseAccounts.reduce((acc, a) => acc + a.balance, 0);

    return (
        <div className="space-y-8 animate-fade-in">
            {/* Balance Check Banner */}
            {!isBalanced ? (
                <div className="bg-red-50 border border-red-100 rounded-2xl p-4 flex flex-col sm:flex-row justify-between items-center gap-4 text-red-700 shadow-sm">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-red-100 rounded-full">
                            <AlertCircle className="w-6 h-6 text-red-600" />
                        </div>
                        <span className="font-bold text-lg">Warning: Balance Sheet is out of balance!</span>
                    </div>
                    <div className="font-mono text-xl font-bold bg-white/50 px-4 py-2 rounded-xl border border-red-100/50">
                         Diff: ฿{(Math.abs(assets - totalLiabilitiesEquity)).toLocaleString()}
                    </div>
                </div>
            ) : (
                <div className="bg-emerald-50 border border-emerald-100 text-emerald-800 px-6 py-4 rounded-3xl flex items-center justify-between gap-3 shadow-sm">
                    <div className="flex items-center gap-3">
                        <CheckCircle className="w-6 h-6 text-emerald-600" />
                        <span className="font-bold text-lg">Books are Balanced</span>
                    </div>
                    <span className="text-xs font-bold uppercase tracking-wide bg-emerald-100/50 px-3 py-1 rounded-full text-emerald-700">As of {new Date().toLocaleDateString()}</span>
                </div>
            )}

            {/* 1. Executive Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-white p-5 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition">
                    <div className="flex justify-between items-start mb-2">
                        <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Total Assets</p>
                        <div className="p-2 bg-emerald-50 rounded-xl text-emerald-600"><Landmark className="w-5 h-5" /></div>
                    </div>
                    <h3 className="text-2xl font-bold text-gray-900">฿{assets.toLocaleString()}</h3>
                </div>
                <div className="bg-white p-5 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition">
                    <div className="flex justify-between items-start mb-2">
                        <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Total Liabilities</p>
                        <div className="p-2 bg-red-50 rounded-xl text-red-600"><Wallet className="w-5 h-5" /></div>
                    </div>
                    <h3 className="text-2xl font-bold text-gray-900">฿{liabilities.toLocaleString()}</h3>
                </div>
                <div className="bg-white p-5 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition">
                    <div className="flex justify-between items-start mb-2">
                        <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Total Equity</p>
                        <div className="p-2 bg-blue-50 rounded-xl text-blue-600"><PieChart className="w-5 h-5" /></div>
                    </div>
                    <h3 className="text-2xl font-bold text-gray-900">฿{equity.toLocaleString()}</h3>
                </div>
                <div className="bg-white p-5 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition">
                    <div className="flex justify-between items-start mb-2">
                        <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Net Profit (YTD)</p>
                        <div className="p-2 bg-yellow-50 rounded-xl text-yellow-600"><TrendingUp className="w-5 h-5" /></div>
                    </div>
                    <h3 className={`text-2xl font-bold ${netProfit >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                        {netProfit >= 0 ? '+' : ''}฿{netProfit.toLocaleString()}
                    </h3>
                </div>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                {/* Left Col: Detailed Balance Sheet */}
                <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden flex flex-col h-full">
                    <div className="p-6 border-b border-gray-100 bg-gray-50/30 flex justify-between items-center">
                         <h3 className="font-bold text-gray-800 flex items-center gap-2">
                            <Landmark className="w-5 h-5 text-gray-500"/> Statement of Financial Position
                         </h3>
                         <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider bg-white border border-gray-200 px-2 py-1 rounded-md">Balance Sheet</span>
                    </div>
                    
                    <div className="p-6 space-y-8 flex-1">
                        {/* Assets Section */}
                        <div className="space-y-3">
                            <div className="flex items-center justify-between">
                                <h4 className="text-sm font-bold text-gray-800 uppercase tracking-wide flex items-center gap-2">
                                    <div className="w-2 h-2 rounded-full bg-emerald-500"></div> Assets
                                </h4>
                                <span className="text-sm font-bold text-gray-900">฿{assets.toLocaleString()}</span>
                            </div>
                            <div className="pl-4 border-l-2 border-gray-100 space-y-2">
                                <div className="flex justify-between text-sm group">
                                    <span className="text-gray-500 group-hover:text-emerald-600 transition-colors">Cash & Equivalents</span>
                                    <span className="font-mono text-gray-700 font-medium">฿{totalCashAssets.toLocaleString()}</span>
                                </div>
                                {cashAssets.map(a => (
                                     <div key={a.code} className="flex justify-between text-xs pl-4 text-gray-400">
                                        <span>• {a.name}</span>
                                        <span>{a.balance.toLocaleString()}</span>
                                     </div>
                                ))}
                                <div className="flex justify-between text-sm group pt-2">
                                    <span className="text-gray-500 group-hover:text-emerald-600 transition-colors">Loans Receivable</span>
                                    <span className="font-mono text-gray-700 font-medium">฿{totalLoansRemaining.toLocaleString()}</span>
                                </div>
                            </div>
                        </div>

                        {/* Liabilities Section */}
                        <div className="space-y-3">
                            <div className="flex items-center justify-between">
                                <h4 className="text-sm font-bold text-gray-800 uppercase tracking-wide flex items-center gap-2">
                                    <div className="w-2 h-2 rounded-full bg-red-500"></div> Liabilities
                                </h4>
                                <span className="text-sm font-bold text-gray-900">฿{liabilities.toLocaleString()}</span>
                            </div>
                            <div className="pl-4 border-l-2 border-gray-100 space-y-2">
                                {otherLiabilities.map(a => (
                                     <div key={a.code} className="flex justify-between text-xs pl-4 text-gray-400">
                                        <span>• {a.name}</span>
                                        <span>{a.balance.toLocaleString()}</span>
                                     </div>
                                ))}
                            </div>
                        </div>

                        {/* Equity Section */}
                         <div className="space-y-3">
                            <div className="flex items-center justify-between">
                                <h4 className="text-sm font-bold text-gray-800 uppercase tracking-wide flex items-center gap-2">
                                    <div className="w-2 h-2 rounded-full bg-blue-500"></div> Equity
                                </h4>
                                <span className="text-sm font-bold text-gray-900">฿{equity.toLocaleString()}</span>
                            </div>
                            <div className="pl-4 border-l-2 border-gray-100 space-y-2">
                                {otherEquity.map(a => (
                                     <div key={a.code} className="flex justify-between text-xs pl-4 text-gray-400">
                                        <span>• {a.name}</span>
                                        <span>{a.balance.toLocaleString()}</span>
                                     </div>
                                ))}
                                <div className="flex justify-between text-sm group pt-2 border-t border-dashed border-gray-200 mt-2">
                                    <span className="text-gray-800 font-bold">Net Profit (Current Year)</span>
                                    <span className={`font-mono font-bold ${netProfit >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>฿{netProfit.toLocaleString()}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Col: Income Statement & Analysis */}
                <div className="space-y-6 flex flex-col h-full">
                     {/* Income Statement Card */}
                     <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
                        <div className="p-6 border-b border-gray-100 bg-gray-50/30 flex justify-between items-center">
                            <h3 className="font-bold text-gray-800 flex items-center gap-2">
                                <TrendingUp className="w-5 h-5 text-gray-500"/> Statement of Performance
                            </h3>
                             <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider bg-white border border-gray-200 px-2 py-1 rounded-md">Income Statement</span>
                        </div>
                         
                        <div className="p-6 space-y-4">
                            {/* Revenue */}
                            <div className="bg-emerald-50/50 rounded-xl p-4 border border-emerald-100/50">
                                <div className="flex justify-between items-center mb-2">
                                    <span className="text-sm font-bold text-emerald-800 uppercase">Total Revenue</span>
                                    <span className="text-lg font-bold text-emerald-700">฿{revenue.toLocaleString()}</span>
                                </div>
                                <div className="space-y-1">
                                    {revenueAccounts.map(a => (
                                        <div key={a.code} className="flex justify-between text-xs text-gray-500">
                                            <span>{a.name} ({a.code})</span>
                                            <span>{a.balance.toLocaleString()}</span>
                                        </div>
                                    ))}
                                    {revenueAccounts.length === 0 && <div className="text-xs text-gray-400 italic">No revenue recorded</div>}
                                </div>
                            </div>

                            {/* Expenses */}
                            <div className="bg-red-50/50 rounded-xl p-4 border border-red-100/50">
                                <div className="flex justify-between items-center mb-2">
                                    <span className="text-sm font-bold text-red-800 uppercase">Total Expenses</span>
                                    <span className="text-lg font-bold text-red-700">-฿{expenses.toLocaleString()}</span>
                                </div>
                                <div className="space-y-1">
                                    {expenseAccounts.map(a => (
                                        <div key={a.code} className="flex justify-between text-xs text-gray-500">
                                            <span>{a.name} ({a.code})</span>
                                            <span>{a.balance.toLocaleString()}</span>
                                        </div>
                                    ))}
                                    {expenseAccounts.length === 0 && <div className="text-xs text-gray-400 italic">No expenses recorded</div>}
                                </div>
                            </div>

                            <div className="border-t-2 border-dashed border-gray-200 pt-4 mt-2">
                                <div className="flex justify-between items-end">
                                    <span className="text-lg font-bold text-gray-800">Net Profit</span>
                                    <span className={`text-3xl font-bold ${netProfit >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                                        ฿{netProfit.toLocaleString()}
                                    </span>
                                </div>
                            </div>
                        </div>
                     </div>

                     {/* Visual Analysis (Charts) */}
                     <div className="grid grid-cols-1 md:grid-cols-2 gap-6 flex-1">
                          {/* Asset Composition Chart */}
                          <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 flex flex-col">
                                <h4 className="font-bold text-gray-800 text-sm mb-4">Asset Composition</h4>
                                <div className="flex-1 min-h-[150px] relative">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <RePieChart>
                                            <Pie
                                                data={assetCompositionData}
                                                cx="50%"
                                                cy="50%"
                                                innerRadius={40}
                                                outerRadius={60}
                                                paddingAngle={5}
                                                dataKey="value"
                                                stroke="none"
                                            >
                                                {assetCompositionData.map((entry, index) => (
                                                    <Cell key={`cell-${index}`} fill={index === 0 ? '#10b981' : '#3b82f6'} />
                                                ))}
                                            </Pie>
                                            <Tooltip contentStyle={{borderRadius: '12px'}} />
                                            <Legend verticalAlign="bottom" height={36} iconType="circle" wrapperStyle={{fontSize: '10px'}}/>
                                        </RePieChart>
                                    </ResponsiveContainer>
                                </div>
                          </div>

                          {/* Performance Chart */}
                           <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 flex flex-col">
                                <h4 className="font-bold text-gray-800 text-sm mb-4">Performance</h4>
                                <div className="flex-1 min-h-[150px]">
                                     <ResponsiveContainer width="100%" height="100%">
                                        <BarChart data={performanceData} margin={{top: 0, right: 0, left: -20, bottom: 0}}>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                                            <XAxis dataKey="name" hide />
                                            <YAxis tick={{fontSize: 10}} axisLine={false} tickLine={false} />
                                            <Tooltip cursor={{fill: 'transparent'}} contentStyle={{borderRadius: '12px'}} />
                                            <Bar dataKey="income" name="Income" fill="#10b981" radius={[4, 4, 0, 0]} barSize={30} />
                                            <Bar dataKey="expense" name="Expense" fill="#ef4444" radius={[4, 4, 0, 0]} barSize={30} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                           </div>
                     </div>
                </div>
            </div>
        </div>
    );
};
