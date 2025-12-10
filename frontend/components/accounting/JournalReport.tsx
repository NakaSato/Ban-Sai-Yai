
import React, { useState } from 'react';
import { Transaction } from '../../types';
import { Plus, Search, Filter, X, Printer } from 'lucide-react';

interface JournalReportProps {
    transactions: Transaction[];
    canRecord: boolean;
    onRecordNew: () => void;
    onPrintReceipt: (t: Transaction) => void;
}

export const JournalReport: React.FC<JournalReportProps> = ({ transactions, canRecord, onRecordNew, onPrintReceipt }) => {
    const [journalSearch, setJournalSearch] = useState('');
    const [journalTypeFilter, setJournalTypeFilter] = useState('ALL');
    const [journalStartDate, setJournalStartDate] = useState('');
    const [journalEndDate, setJournalEndDate] = useState('');

    const filteredTransactions = transactions.filter(t => {
        const searchLower = journalSearch.toLowerCase();
        const matchesSearch = t.description.toLowerCase().includes(searchLower) ||
                              t.id.toLowerCase().includes(searchLower) ||
                              (t.category && t.category.toLowerCase().includes(searchLower));
        const matchesType = journalTypeFilter === 'ALL' || t.type === journalTypeFilter;
        const matchesDate = (!journalStartDate || t.date >= journalStartDate) &&
                            (!journalEndDate || t.date <= journalEndDate);
        return matchesSearch && matchesType && matchesDate;
    });
  
    const transactionTypes = Array.from(new Set(transactions.map(t => t.type))).concat(['INCOME', 'EXPENSE', 'FINE']);
    const uniqueTypes = Array.from(new Set(transactionTypes)) as string[];

    return (
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden animate-fade-in print:shadow-none print:border-none print:rounded-none">
            <div className="p-6 border-b border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 print:hidden">
                <div>
                    <h3 className="text-lg font-bold text-gray-800">Income-Expense Report</h3>
                    <p className="text-sm text-gray-500">Transaction log for receipts and payments</p>
                </div>
                {canRecord && (
                <button 
                    onClick={onRecordNew}
                    className="w-full sm:w-auto flex items-center justify-center space-x-2 px-5 py-2.5 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 transition shadow-lg shadow-emerald-200 font-bold"
                >
                    <Plus className="w-5 h-5" />
                    <span>Record Income/Expense</span>
                </button>
                )}
            </div>

            {/* Filters - Hidden on Print */}
            <div className="p-4 border-b border-gray-100 bg-gray-50/50 print:hidden">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                        type="text"
                        placeholder="Search description, ID, category..."
                        className="w-full pl-9 pr-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-emerald-500 transition-all shadow-sm"
                        value={journalSearch}
                        onChange={(e) => setJournalSearch(e.target.value)}
                    />
                </div>

                <div className="relative">
                    <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <select
                        className="w-full pl-9 pr-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-emerald-500 appearance-none cursor-pointer shadow-sm"
                        value={journalTypeFilter}
                        onChange={(e) => setJournalTypeFilter(e.target.value)}
                    >
                        <option value="ALL">All Types</option>
                        {uniqueTypes.map(type => (
                            <option key={type} value={type}>{type.replace('_', ' ')}</option>
                        ))}
                    </select>
                </div>

                <div className="flex space-x-2 md:col-span-2">
                    <input
                        type="date"
                        className="flex-1 w-full px-2 py-2.5 bg-white border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-emerald-500 text-gray-600 shadow-sm"
                        value={journalStartDate}
                        onChange={(e) => setJournalStartDate(e.target.value)}
                    />
                    <input
                        type="date"
                        className="flex-1 w-full px-2 py-2.5 bg-white border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-emerald-500 text-gray-600 shadow-sm"
                        value={journalEndDate}
                        onChange={(e) => setJournalEndDate(e.target.value)}
                    />
                    {(journalSearch || journalTypeFilter !== 'ALL' || journalStartDate || journalEndDate) && (
                        <button
                            onClick={() => {
                                setJournalSearch('');
                                setJournalTypeFilter('ALL');
                                setJournalStartDate('');
                                setJournalEndDate('');
                            }}
                            className="p-2.5 text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-xl transition shrink-0 bg-white border border-gray-200 shadow-sm"
                        >
                            <X className="w-5 h-5" />
                        </button>
                    )}
                </div>
            </div>
            </div>
            
            <div className="hidden md:block overflow-x-auto print:block">
                <table className="w-full text-left">
                    <thead className="bg-gray-50/50 text-gray-600 text-xs uppercase font-bold print:bg-white print:border-b-2 print:border-black">
                        <tr>
                            <th className="p-4 print:p-2">Date</th>
                            <th className="p-4 print:p-2">Txn ID</th>
                            <th className="p-4 print:p-2">Type</th>
                            <th className="p-4 print:p-2">Category</th>
                            <th className="p-4 print:p-2">Description</th>
                            <th className="p-4 text-right print:p-2">Amount</th>
                            <th className="p-4 text-center print:hidden">Receipt</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 text-sm print:divide-gray-300">
                        {filteredTransactions.map((t) => (
                            <tr key={t.id} className="hover:bg-gray-50 transition print:hover:bg-transparent">
                                <td className="p-4 text-gray-500 print:text-black print:p-2">{t.date}</td>
                                <td className="p-4 font-mono text-gray-400 print:text-black print:p-2">{t.id}</td>
                                <td className="p-4 print:p-2">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold uppercase border print:border-none print:px-0 print:text-black ${
                                        t.type === 'DEPOSIT' || t.type === 'SHARE_PURCHASE' || t.type === 'LOAN_REPAYMENT' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' : 
                                        t.type === 'INCOME' ? 'bg-blue-50 text-blue-700 border-blue-100' :
                                        'bg-red-50 text-red-700 border-red-100'
                                    }`}>
                                        {t.type.replace('_', ' ')}
                                    </span>
                                </td>
                                <td className="p-4 print:p-2">
                                    <span className="bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs font-bold print:bg-transparent print:px-0 print:text-black">{t.category || '-'}</span>
                                </td>
                                <td className="p-4 text-gray-700 font-medium print:text-black print:p-2">{t.description}</td>
                                <td className={`p-4 text-right font-bold print:text-black print:p-2 ${
                                    t.type === 'WITHDRAWAL' || t.type === 'LOAN_DISBURSEMENT' || t.type === 'EXPENSE' ? 'text-red-600' :
                                    t.type === 'FINE' ? 'text-orange-600' : 'text-emerald-600'
                                }`}>
                                    {t.type === 'WITHDRAWAL' || t.type === 'LOAN_DISBURSEMENT' || t.type === 'EXPENSE' ? '-' : 
                                    t.type === 'FINE' ? '-' : '+'}฿{t.amount.toLocaleString()}
                                </td>
                                <td className="p-4 text-center print:hidden">
                                    <button onClick={() => onPrintReceipt(t)} className="text-gray-400 hover:text-gray-600 transition" title="Print Receipt">
                                        <Printer className="w-4 h-4" />
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {filteredTransactions.length === 0 && (
                            <tr>
                                <td colSpan={7} className="p-8 text-center text-gray-500">
                                    No transactions found matching your criteria.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
