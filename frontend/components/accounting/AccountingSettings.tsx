
import React, { useState } from 'react';
import { Account } from '../../types';
import { Settings, Plus, Edit3, Trash2, RefreshCw, AlertTriangle, CheckCircle, Lock, Printer, Landmark } from 'lucide-react';

interface AccountingSettingsProps {
    accounts: Account[];
    canManage: boolean;
    onAddAccountClick: () => void;
    onDeleteAccount: (code: string) => void;
    onPrintClosingReport: (data: any) => void;
    revenue: number;
    expenses: number;
    netProfit: number;
}

export const AccountingSettings: React.FC<AccountingSettingsProps> = ({ 
    accounts, canManage, onAddAccountClick, onDeleteAccount, onPrintClosingReport,
    revenue, expenses, netProfit
}) => {
    // Local state for closing wizard
    const [closingStep, setClosingStep] = useState(0); 
    const [simulationData, setSimulationData] = useState<any>(null);

    const runSimulation = () => {
        setClosingStep(1);
        // Mock calculation delay
        setTimeout(() => {
            setSimulationData({
                activeLoans: 150,
                totalIncome: revenue, 
                totalExpense: expenses,
                netBalance: netProfit,
                broughtForward: 1250000, 
                exceptions: [
                    { id: 'L099', issue: 'Negative Balance', amount: -50 }
                ]
            });
        }, 800);
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 animate-fade-in print:hidden">
            {/* 1. Chart of Accounts Manager */}
            <div className="lg:col-span-2 bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                    <div>
                        <h3 className="text-lg font-bold text-gray-800">Chart of Accounts</h3>
                        <p className="text-sm text-gray-500">Manage standard account codes</p>
                    </div>
                    {canManage && (
                        <button 
                        onClick={onAddAccountClick}
                        className="flex items-center space-x-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition text-sm font-bold"
                        >
                            <Plus className="w-4 h-4" /> <span>Add Account</span>
                        </button>
                    )}
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-gray-50/50 text-xs uppercase text-gray-500 font-bold">
                            <tr>
                                <th className="p-4">Code</th>
                                <th className="p-4">Account Name</th>
                                <th className="p-4">Category</th>
                                {canManage && <th className="p-4 text-center">Action</th>}
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 text-sm">
                            {accounts.sort((a,b) => a.code.localeCompare(b.code)).map((acc) => (
                                <tr key={acc.code} className="hover:bg-gray-50 transition group">
                                    <td className="p-4 font-mono text-gray-600 font-medium">{acc.code}</td>
                                    <td className="p-4 font-bold text-gray-800">{acc.name}</td>
                                    <td className="p-4"><span className="px-2.5 py-1 bg-gray-100 text-gray-600 rounded-full text-xs font-medium">{acc.category}</span></td>
                                    {canManage && (
                                        <td className="p-4 text-center">
                                            <div className="flex justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                                <button className="text-blue-600 hover:text-blue-800 p-1.5 hover:bg-blue-50 rounded-lg transition" title="Edit">
                                                    <Edit3 className="w-4 h-4" />
                                                </button>
                                                <button 
                                                onClick={() => onDeleteAccount(acc.code)}
                                                className="text-red-600 hover:text-red-800 p-1.5 hover:bg-red-50 rounded-lg transition" 
                                                title="Delete"
                                                >
                                                    <Trash2 className="w-4 h-4" />
                                                </button>
                                            </div>
                                        </td>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* 2. Monthly Closing Wizard */}
            <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 h-fit">
                <h3 className="text-lg font-bold text-gray-800 mb-6 flex items-center gap-2">
                    <Settings className="w-5 h-5 text-gray-500" /> Period Closing
                </h3>
                
                {canManage ? (
                    <div className="space-y-4">
                        {/* Wizard Steps */}
                        {closingStep === 0 && (
                            <div className="p-5 bg-orange-50 rounded-2xl border border-orange-100">
                            <h4 className="font-bold text-orange-900 mb-2">Monthly Close: Oct 2023</h4>
                            <p className="text-sm text-orange-700 mb-4 leading-relaxed">
                                Ensure all transactions are recorded. This will lock the period and carry forward balances.
                            </p>
                            <button onClick={runSimulation} className="w-full py-2.5 bg-orange-600 text-white rounded-xl hover:bg-orange-700 transition shadow-sm font-bold">
                                Start Closing Process
                            </button>
                            </div>
                        )}

                        {closingStep === 1 && (
                            <div className="p-5 bg-gray-50 rounded-2xl border border-gray-100">
                                <h4 className="font-bold text-gray-800 mb-3 flex items-center gap-2">
                                    <RefreshCw className={`w-4 h-4 ${simulationData ? '' : 'animate-spin'}`} />
                                    {simulationData ? 'Dry Run Results' : 'Simulating...'}
                                </h4>
                                
                                {simulationData ? (
                                    <div className="space-y-3 mb-4">
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Active Loans:</span>
                                            <span className="font-bold text-gray-800">{simulationData.activeLoans}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Total Income:</span>
                                            <span className="font-bold text-emerald-600">฿{simulationData.totalIncome.toLocaleString()}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Total Expenses:</span>
                                            <span className="font-bold text-red-600">฿{simulationData.totalExpense.toLocaleString()}</span>
                                        </div>
                                        <div className="flex justify-between text-sm border-t border-gray-200 pt-2 mt-2">
                                            <span className="text-gray-800 font-bold">Net Balance:</span>
                                            <span className={`font-bold ${simulationData.netBalance >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                                                ฿{simulationData.netBalance.toLocaleString()}
                                            </span>
                                        </div>
                                        
                                        <div className="pt-3 mt-2">
                                            <button onClick={() => setClosingStep(2)} className="w-full py-2.5 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 font-bold transition">
                                                Next: Check Exceptions
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="h-24 flex items-center justify-center text-xs text-gray-400">
                                        Calculation in progress...
                                    </div>
                                )}
                            </div>
                        )}

                        {closingStep === 2 && (
                            <div className="p-5 bg-gray-50 rounded-2xl border border-gray-100">
                                <h4 className="font-bold text-gray-800 mb-3 flex items-center gap-2">
                                    <AlertTriangle className="w-4 h-4 text-orange-500" /> Exception Check
                                </h4>
                                {simulationData?.exceptions.length > 0 ? (
                                    <div className="space-y-3 mb-4">
                                        {simulationData.exceptions.map((ex: any, i: number) => (
                                            <div key={i} className="bg-red-50 p-3 rounded-xl border border-red-100 flex justify-between items-center text-sm">
                                                <span className="text-red-800 font-medium">{ex.id}: {ex.issue}</span>
                                                <span className="font-bold text-red-600">฿{ex.amount}</span>
                                            </div>
                                        ))}
                                        <p className="text-xs text-gray-500 italic mt-2">Please resolve issues before closing.</p>
                                        <div className="pt-2 flex gap-2">
                                            <button onClick={() => setClosingStep(0)} className="flex-1 py-2 bg-gray-200 text-gray-700 rounded-xl hover:bg-gray-300 font-bold text-xs">
                                                Cancel
                                            </button>
                                            <button onClick={() => setClosingStep(3)} className="flex-1 py-2 bg-red-600 text-white rounded-xl hover:bg-red-700 font-bold text-xs">
                                                Force Commit
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="text-center py-4">
                                        <CheckCircle className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                                        <p className="text-sm font-bold text-emerald-700">No Exceptions Found</p>
                                        <button onClick={() => setClosingStep(3)} className="w-full mt-4 py-2.5 bg-emerald-600 text-white rounded-xl font-bold">
                                            Confirm & Close
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}

                        {closingStep === 3 && (
                            <div className="p-6 bg-green-50 rounded-2xl border border-green-100 text-center animate-in fade-in zoom-in">
                                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                                    <Lock className="w-6 h-6 text-green-600" />
                                </div>
                                <h4 className="font-bold text-green-800 text-lg">Period Closed</h4>
                                <p className="text-sm text-green-600 mb-4">October 2023 is now locked. Balances carried forward.</p>
                                <div className="space-y-3">
                                    <button 
                                    onClick={() => onPrintClosingReport(simulationData)}
                                    className="w-full py-2.5 bg-white border border-green-200 text-green-700 rounded-xl font-bold hover:bg-green-50 transition flex items-center justify-center gap-2 shadow-sm"
                                    >
                                        <Printer className="w-4 h-4" /> Print Closing Certificate
                                    </button>
                                    <button onClick={() => setClosingStep(0)} className="text-sm underline text-green-700 font-medium hover:text-green-800">
                                        Return to Menu
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center py-8 text-center text-gray-500">
                        <Landmark className="w-12 h-12 mb-3 text-gray-300" />
                        <p>Restricted Access</p>
                        <p className="text-xs">Only the Secretary can manage accounts.</p>
                    </div>
                )}
            </div>
        </div>
    );
};
