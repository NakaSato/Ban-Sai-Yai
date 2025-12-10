
import React, { useState } from 'react';
import { Account } from '../../types';

interface AccountModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (account: Partial<Account>) => void;
}

export const AccountModal: React.FC<AccountModalProps> = ({ isOpen, onClose, onSave }) => {
    const [newAccount, setNewAccount] = useState<Partial<Account>>({
        code: '',
        name: '',
        category: 'EXPENSE',
        balance: 0
    });

    const handleSave = () => {
        if (!newAccount.code || !newAccount.name) return;
        onSave(newAccount);
        setNewAccount({ code: '', name: '', category: 'EXPENSE', balance: 0 });
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 backdrop-blur-sm print:hidden">
            <div className="bg-white rounded-3xl shadow-xl max-w-sm w-full p-8 animate-fade-in">
                <h3 className="text-xl font-bold text-gray-800 mb-6">Add New Account</h3>
                <div className="space-y-4">
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Account Code</label>
                        <input 
                        type="text" 
                        className="w-full border border-gray-300 rounded-xl p-3 outline-none focus:ring-2 focus:ring-emerald-500 transition" 
                        placeholder="e.g. 5103"
                        value={newAccount.code}
                        onChange={(e) => setNewAccount({...newAccount, code: e.target.value})}
                        />
                    </div>
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Account Name</label>
                        <input 
                        type="text" 
                        className="w-full border border-gray-300 rounded-xl p-3 outline-none focus:ring-2 focus:ring-emerald-500 transition" 
                        placeholder="e.g. Internet Bill"
                        value={newAccount.name}
                        onChange={(e) => setNewAccount({...newAccount, name: e.target.value})}
                        />
                    </div>
                    <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Category</label>
                        <select 
                        className="w-full border border-gray-300 rounded-xl p-3 bg-white outline-none focus:ring-2 focus:ring-emerald-500 transition"
                        value={newAccount.category}
                        onChange={(e) => setNewAccount({...newAccount, category: e.target.value as any})}
                        >
                            <option value="ASSET">Asset (สินทรัพย์)</option>
                            <option value="LIABILITY">Liability (หนี้สิน)</option>
                            <option value="EQUITY">Equity (ทุน)</option>
                            <option value="REVENUE">Revenue (รายรับ)</option>
                            <option value="EXPENSE">Expense (รายจ่าย)</option>
                        </select>
                    </div>
                </div>
                <div className="flex gap-3 mt-8">
                    <button onClick={onClose} className="flex-1 py-2.5 text-gray-600 hover:bg-gray-100 rounded-xl font-medium transition">Cancel</button>
                    <button onClick={handleSave} className="flex-1 py-2.5 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 font-bold shadow-lg shadow-emerald-200 transition">Save</button>
                </div>
            </div>
        </div>
    );
};
