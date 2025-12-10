
import React, { useState } from 'react';
import { Account, Transaction } from '../../types';
import { ArrowDownLeft, ArrowUpRight } from 'lucide-react';

interface TransactionModalProps {
    isOpen: boolean;
    onClose: () => void;
    accounts: Account[];
    onSave: (transaction: Partial<Transaction>) => void;
}

export const TransactionModal: React.FC<TransactionModalProps> = ({ isOpen, onClose, accounts, onSave }) => {
    const [newTrans, setNewTrans] = useState<{
        type: 'INCOME' | 'EXPENSE';
        date: string;
        categoryCode: string;
        description: string;
        amount: string;
    }>({
        type: 'EXPENSE',
        date: new Date().toISOString().split('T')[0],
        categoryCode: '',
        description: '',
        amount: ''
    });

    const handleSubmit = () => {
        if (!newTrans.amount || !newTrans.categoryCode) return;
        
        const category = accounts.find(a => a.code === newTrans.categoryCode);
        const transaction: Partial<Transaction> = {
            date: newTrans.date,
            type: newTrans.type,
            category: category?.name,
            description: newTrans.description || category?.name || 'General Transaction',
            amount: parseFloat(newTrans.amount),
            receiptId: `REC-${Date.now().toString().slice(-6)}`
        };
        
        onSave(transaction);
        setNewTrans({ type: 'EXPENSE', date: new Date().toISOString().split('T')[0], categoryCode: '', description: '', amount: '' });
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 backdrop-blur-sm print:hidden">
          <div className="bg-white rounded-3xl shadow-xl max-w-md w-full p-8 animate-fade-in">
            <h3 className="text-xl font-bold text-gray-800 mb-6">Record General Transaction</h3>
            <div className="space-y-6">
                
                {/* Visual Toggle Switch */}
                <div className="grid grid-cols-2 gap-4">
                    <button 
                        className={`py-4 rounded-2xl flex flex-col items-center justify-center gap-2 border-2 transition-all ${
                            newTrans.type === 'INCOME' 
                            ? 'bg-emerald-50 border-emerald-500 text-emerald-700 shadow-sm ring-1 ring-emerald-500' 
                            : 'bg-white border-gray-200 text-gray-400 hover:bg-gray-50'
                        }`}
                        onClick={() => setNewTrans({...newTrans, type: 'INCOME'})}
                    >
                        <div className={`p-2 rounded-full ${newTrans.type === 'INCOME' ? 'bg-emerald-200' : 'bg-gray-100'}`}>
                            <ArrowDownLeft className="w-6 h-6" />
                        </div>
                        <span className="font-bold text-sm">INCOME (รับ)</span>
                    </button>

                    <button 
                        className={`py-4 rounded-2xl flex flex-col items-center justify-center gap-2 border-2 transition-all ${
                            newTrans.type === 'EXPENSE' 
                            ? 'bg-red-50 border-red-500 text-red-700 shadow-sm ring-1 ring-red-500' 
                            : 'bg-white border-gray-200 text-gray-400 hover:bg-gray-50'
                        }`}
                        onClick={() => setNewTrans({...newTrans, type: 'EXPENSE'})}
                    >
                        <div className={`p-2 rounded-full ${newTrans.type === 'EXPENSE' ? 'bg-red-200' : 'bg-gray-100'}`}>
                            <ArrowUpRight className="w-6 h-6" />
                        </div>
                        <span className="font-bold text-sm">EXPENSE (จ่าย)</span>
                    </button>
                </div>
                
                <div className="space-y-4">
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Date</label>
                        <input type="date" className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-gray-400 transition" value={newTrans.date} onChange={e => setNewTrans({...newTrans, date: e.target.value})} />
                    </div>
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Account Category</label>
                        <select 
                            className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-gray-400 bg-white transition"
                            value={newTrans.categoryCode}
                            onChange={e => setNewTrans({...newTrans, categoryCode: e.target.value})}
                        >
                            <option value="">Select Account...</option>
                            {accounts.filter(a => newTrans.type === 'INCOME' ? a.category === 'REVENUE' : a.category === 'EXPENSE').map(a => (
                                <option key={a.code} value={a.code}>{a.code} - {a.name}</option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Description</label>
                        <input type="text" className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-gray-400 transition" placeholder="e.g. Office Paper" value={newTrans.description} onChange={e => setNewTrans({...newTrans, description: e.target.value})} />
                    </div>
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Amount</label>
                        <div className="relative">
                            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-lg font-bold text-gray-400">฿</span>
                            <input 
                                type="number" 
                                className={`w-full border rounded-xl pl-10 pr-4 py-3 outline-none focus:ring-2 transition text-xl font-bold ${newTrans.type === 'INCOME' ? 'text-emerald-600 border-emerald-200 focus:ring-emerald-500' : 'text-red-600 border-red-200 focus:ring-red-500'}`} 
                                placeholder="0.00" 
                                value={newTrans.amount} 
                                onChange={e => setNewTrans({...newTrans, amount: e.target.value})} 
                            />
                        </div>
                    </div>
                </div>
            </div>
            <div className="flex gap-3 mt-8">
                <button onClick={onClose} className="flex-1 py-3 border border-gray-200 rounded-xl text-gray-600 hover:bg-gray-50 font-medium transition">Cancel</button>
                <button 
                    onClick={handleSubmit} 
                    className={`flex-1 py-3 text-white rounded-xl font-bold shadow-lg transition ${newTrans.type === 'INCOME' ? 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-200' : 'bg-red-600 hover:bg-red-700 shadow-red-200'}`}
                >
                    Save Record
                </button>
            </div>
          </div>
        </div>
    );
};
