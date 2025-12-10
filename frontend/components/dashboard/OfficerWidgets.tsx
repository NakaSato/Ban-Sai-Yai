
import React from 'react';
import { 
  UserPlus, PiggyBank, Receipt, ArrowLeftRight, History, 
  ArrowDownLeft, ArrowUpRight, ClipboardList, ArrowRight, Plus
} from 'lucide-react';
import { ViewState, Transaction } from '../../types';

// --- Quick Actions ---
interface OfficerQuickActionsProps {
    onNavigate: (view: ViewState) => void;
}

export const OfficerQuickActions: React.FC<OfficerQuickActionsProps> = ({ onNavigate }) => {
    return (
        <div className="pt-2">
            <h3 className="text-gray-500 font-bold mb-4 text-xs uppercase tracking-wider pl-1">
                Quick Actions
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                <ActionCard 
                    icon={UserPlus} 
                    label="Register Member" 
                    color="text-emerald-600" 
                    bg="bg-emerald-50" 
                    hoverBg="group-hover:bg-emerald-500"
                    onClick={() => onNavigate('MEMBERS')} 
                />
                <ActionCard 
                    icon={PiggyBank} 
                    label="Deposit / Shares" 
                    color="text-blue-600" 
                    bg="bg-blue-50" 
                    hoverBg="group-hover:bg-blue-500"
                    onClick={() => onNavigate('MEMBERS')} 
                />
                <ActionCard 
                    icon={Receipt} 
                    label="Loan Repayment" 
                    color="text-indigo-600" 
                    bg="bg-indigo-50" 
                    hoverBg="group-hover:bg-indigo-500"
                    onClick={() => onNavigate('LOANS')} 
                />
                <ActionCard 
                    icon={ArrowLeftRight} 
                    label="Income / Expenses" 
                    color="text-orange-600" 
                    bg="bg-orange-50" 
                    hoverBg="group-hover:bg-orange-500"
                    onClick={() => onNavigate('ACCOUNTING')} 
                />
            </div>
        </div>
    );
};

const ActionCard: React.FC<any> = ({ icon: Icon, label, color, bg, hoverBg, onClick }) => (
    <button 
        onClick={onClick}
        className="bg-white p-6 rounded-3xl shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 flex flex-col items-center justify-center space-y-4 group border border-gray-100/60 h-40"
    >
        <div className={`p-4 ${bg} ${color} rounded-2xl ${hoverBg} group-hover:text-white transition-all duration-300 shadow-sm`}>
            <Icon className="w-8 h-8" />
        </div>
        <span className="font-bold text-gray-700 group-hover:text-gray-900">{label}</span>
    </button>
);

// --- Recent Transactions ---
interface OfficerTransactionsProps {
    transactions: Transaction[];
}

export const OfficerTransactions: React.FC<OfficerTransactionsProps> = ({ transactions }) => {
    return (
        <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 flex flex-col h-full">
            <div className="flex justify-between items-center mb-6">
                <div className="flex items-center space-x-3">
                    <div className="p-2 bg-gray-50 rounded-xl border border-gray-200">
                        <History className="w-5 h-5 text-gray-500" />
                    </div>
                    <h3 className="font-bold text-lg text-gray-800">Recent Transactions</h3>
                </div>
                <button className="text-xs font-bold text-emerald-600 hover:text-emerald-700 hover:underline">View All</button>
            </div>
            
            <div className="overflow-x-auto flex-1">
                <table className="w-full text-left">
                    <thead className="text-[11px] uppercase text-gray-400 font-bold tracking-wider border-b border-gray-50">
                        <tr>
                            <th className="py-3">Time</th>
                            <th className="py-3">Type</th>
                            <th className="py-3">Member</th>
                            <th className="py-3 text-right">Amount</th>
                        </tr>
                    </thead>
                    <tbody className="text-sm">
                        {transactions.map((t) => (
                            <tr key={t.id} className="group hover:bg-gray-50 transition-colors">
                                <td className="py-4 font-mono text-gray-400 text-xs">{t.date}</td>
                                <td className="py-4">
                                    <span className={`px-3 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wide ${
                                        t.type === 'DEPOSIT' ? 'bg-green-100 text-green-700' :
                                        t.type === 'WITHDRAWAL' ? 'bg-red-100 text-red-700' :
                                        t.type === 'FINE' ? 'bg-orange-100 text-orange-700' :
                                        'bg-blue-100 text-blue-700'
                                    }`}>
                                        {t.type.replace('_', ' ')}
                                    </span>
                                </td>
                                <td className="py-4 font-semibold text-gray-700">Member {t.memberId || '-'}</td>
                                <td className="py-4 text-right font-bold text-gray-800">฿{t.amount.toLocaleString()}</td>
                            </tr>
                        ))}
                        {transactions.length === 0 && (
                            <tr><td colSpan={4} className="py-8 text-center text-gray-400">No transactions today.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

// --- Pending Tasks ---
interface OfficerPendingTasksProps {
    tasks: any[];
}

export const OfficerPendingTasks: React.FC<OfficerPendingTasksProps> = ({ tasks }) => {
    return (
        <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 flex flex-col h-full">
            <div className="flex items-center space-x-3 mb-6">
                <div className="p-2 bg-gray-50 rounded-xl border border-gray-200">
                    <ClipboardList className="w-5 h-5 text-gray-500" />
                </div>
                <h3 className="font-bold text-lg text-gray-800">Pending Tasks</h3>
            </div>
            
            <div className="space-y-4 flex-1">
                {tasks.map((task) => (
                    <div key={task.id} className="p-4 rounded-2xl border border-gray-100 hover:border-emerald-200 hover:shadow-md transition-all cursor-pointer bg-white group">
                        <div className="flex justify-between items-start mb-2">
                            <span className={`text-[10px] uppercase font-bold px-2 py-1 rounded-md ${
                                task.type === 'URGENT' ? 'bg-red-50 text-red-600' : 'bg-yellow-50 text-yellow-600'
                            }`}>
                                {task.type}
                            </span>
                            <ArrowRight className="w-4 h-4 text-gray-300 group-hover:text-emerald-500 transition-colors" />
                        </div>
                        <p className="text-sm font-medium text-gray-700 leading-relaxed pr-2">
                            {task.task}
                        </p>
                    </div>
                ))}
                
                 {/* Placeholder for empty state or more tasks */}
                 <div className="p-4 rounded-2xl border border-dashed border-gray-200 hover:bg-gray-50 transition-colors cursor-pointer flex items-center justify-center text-gray-400 text-sm font-medium gap-2 min-h-[100px]">
                     <Plus className="w-4 h-4" /> Add Task
                 </div>
            </div>
        </div>
    );
};
