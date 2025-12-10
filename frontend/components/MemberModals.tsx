
import React, { useState, useEffect } from 'react';
import { Member } from '../types';
import { X, Save, AlertCircle, Check, Loader2, User, Calendar, MapPin, Briefcase, Phone, CreditCard, DollarSign } from 'lucide-react';
import { MOCK_MEMBERS } from '../constants';

// --- Share Modal ---
interface ShareModalProps {
    isOpen: boolean;
    onClose: () => void;
    member: Member | null;
    amount: string;
    setAmount: (val: string) => void;
    onConfirm: () => void;
}

export const ShareModal: React.FC<ShareModalProps> = ({ isOpen, onClose, member, amount, setAmount, onConfirm }) => {
    if (!isOpen || !member) return null;
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
            <div className="bg-white rounded-3xl shadow-xl max-w-sm w-full p-8 animate-fade-in">
                <h3 className="text-xl font-bold text-gray-800 mb-2">Buy Shares</h3>
                <p className="text-sm text-gray-500 mb-6">Record for <strong>{member.fullName}</strong>.</p>
                <div className="mb-4">
                    <label className="block text-sm font-bold text-gray-700 mb-1">Amount (฿)</label>
                    <input type="number" className="w-full border border-gray-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-purple-500 font-bold text-2xl text-purple-600 transition" value={amount} onChange={(e) => setAmount(e.target.value)} autoFocus />
                </div>
                <div className="flex gap-3 justify-end">
                    <button onClick={onClose} className="px-5 py-2 text-gray-600 hover:bg-gray-100 rounded-xl font-bold text-sm">Cancel</button>
                    <button onClick={onConfirm} className="px-5 py-2 bg-purple-600 text-white rounded-xl hover:bg-purple-700 font-bold text-sm shadow-lg">Confirm</button>
                </div>
            </div>
        </div>
    );
};

// --- Member Form Modal ---
interface MemberFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    isEditing: boolean;
    formData: Partial<Member>;
    setFormData: (data: Partial<Member>) => void;
    onSave: () => void;
}

export const MemberFormModal: React.FC<MemberFormModalProps> = ({ isOpen, onClose, isEditing, formData, setFormData, onSave }) => {
    const [age, setAge] = useState<number | null>(null);
    const [idCheckStatus, setIdCheckStatus] = useState<'IDLE' | 'CHECKING' | 'VALID' | 'DUPLICATE'>('IDLE');

    // Auto-Calculate Age
    useEffect(() => {
        if (formData.birthDate) {
            const birth = new Date(formData.birthDate);
            const today = new Date();
            let calculatedAge = today.getFullYear() - birth.getFullYear();
            const m = today.getMonth() - birth.getMonth();
            if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
                calculatedAge--;
            }
            setAge(calculatedAge);
        } else {
            setAge(null);
        }
    }, [formData.birthDate]);

    // Duplicate ID Check Simulation
    useEffect(() => {
        if (formData.idCardNumber && formData.idCardNumber.length >= 13) {
            setIdCheckStatus('CHECKING');
            const timer = setTimeout(() => {
                // Mock Check: In real app, this is an API call
                const exists = MOCK_MEMBERS.some(m => m.idCardNumber === formData.idCardNumber && m.id !== formData.id);
                setIdCheckStatus(exists ? 'DUPLICATE' : 'VALID');
            }, 600);
            return () => clearTimeout(timer);
        } else {
            setIdCheckStatus('IDLE');
        }
    }, [formData.idCardNumber, formData.id]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 transition-all duration-300">
            <div className="bg-white rounded-[2rem] shadow-2xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-200">
                
                {/* Header */}
                <div className="px-8 py-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                    <div className="flex items-center gap-4">
                        <div className={`p-3 rounded-2xl ${isEditing ? 'bg-blue-100 text-blue-600' : 'bg-emerald-100 text-emerald-600'}`}>
                            <User className="w-6 h-6" />
                        </div>
                        <div>
                            <h3 className="text-xl font-bold text-gray-900">{isEditing ? 'Edit Profile' : 'New Registration'}</h3>
                            <p className="text-sm text-gray-500 font-medium">
                                {isEditing ? 'Update member details below' : 'Enter details to create a new account'}
                            </p>
                        </div>
                    </div>
                    <button 
                        onClick={onClose} 
                        className="p-2.5 text-gray-400 hover:text-gray-600 hover:bg-white hover:shadow-sm rounded-full transition-all border border-transparent hover:border-gray-100"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>
                
                {/* Scrollable Form Content */}
                <div className="p-8 overflow-y-auto custom-scrollbar flex-1 space-y-8">
                    
                    {/* Section 1: Identity */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                            <CreditCard className="w-4 h-4" /> Identity Verification
                        </h4>
                        
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <div className="col-span-1 md:col-span-2">
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">ID Card Number</label>
                                <div className="relative group">
                                    <input 
                                        type="text" 
                                        className={`w-full bg-gray-50 border-2 rounded-xl px-4 py-3 outline-none focus:bg-white transition-all font-mono tracking-wide text-gray-800 placeholder-gray-400 ${
                                            idCheckStatus === 'DUPLICATE' 
                                            ? 'border-red-100 focus:border-red-500 focus:ring-4 focus:ring-red-500/10' 
                                            : idCheckStatus === 'VALID' 
                                            ? 'border-emerald-100 focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10'
                                            : 'border-transparent focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10'
                                        }`}
                                        value={formData.idCardNumber} 
                                        onChange={e => setFormData({...formData, idCardNumber: e.target.value})} 
                                        placeholder="1-xxxx-xxxxx-xx-x" 
                                        maxLength={13}
                                    />
                                    <div className="absolute right-4 top-1/2 -translate-y-1/2">
                                        {idCheckStatus === 'CHECKING' && <Loader2 className="w-5 h-5 text-gray-400 animate-spin" />}
                                        {idCheckStatus === 'VALID' && <Check className="w-5 h-5 text-emerald-500 bg-emerald-100 rounded-full p-0.5" />}
                                        {idCheckStatus === 'DUPLICATE' && <AlertCircle className="w-5 h-5 text-red-500" />}
                                    </div>
                                </div>
                                {idCheckStatus === 'DUPLICATE' && (
                                    <p className="text-xs text-red-600 mt-2 font-bold flex items-center gap-1">
                                        <AlertCircle className="w-3 h-3" /> Duplicate ID detected in system.
                                    </p>
                                )}
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Full Name</label>
                                <div className="relative">
                                    <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                    <input 
                                        type="text" 
                                        className="w-full bg-gray-50 border-2 border-transparent rounded-xl pl-11 pr-4 py-3 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-medium text-gray-800"
                                        value={formData.fullName} 
                                        onChange={e => setFormData({...formData, fullName: e.target.value})} 
                                        placeholder="Enter full name"
                                    />
                                </div>
                            </div>
                            
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Phone Number</label>
                                <div className="relative">
                                    <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                    <input 
                                        type="tel" 
                                        className="w-full bg-gray-50 border-2 border-transparent rounded-xl pl-11 pr-4 py-3 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-medium text-gray-800 font-mono"
                                        value={formData.phoneNumber} 
                                        onChange={e => setFormData({...formData, phoneNumber: e.target.value})}
                                        placeholder="08x-xxx-xxxx"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="h-px bg-gray-100"></div>

                    {/* Section 2: Personal Details */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                            <MapPin className="w-4 h-4" /> Personal Details
                        </h4>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            {/* Date of Birth & Age */}
                            <div className="p-4 bg-gray-50 rounded-2xl border border-gray-100 space-y-4">
                                <div>
                                    <label className="block text-sm font-bold text-gray-700 mb-1.5">Date of Birth</label>
                                    <div className="relative">
                                        <input 
                                            type="date" 
                                            className="w-full bg-white border border-gray-200 rounded-xl px-4 py-2.5 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition-all text-sm font-medium text-gray-700"
                                            value={formData.birthDate} 
                                            onChange={e => setFormData({...formData, birthDate: e.target.value})} 
                                        />
                                    </div>
                                </div>
                                <div className="flex items-center justify-between bg-white px-4 py-3 rounded-xl border border-gray-200 shadow-sm">
                                    <span className="text-xs font-bold text-gray-400 uppercase">Age</span>
                                    <span className="font-bold text-gray-800 text-lg">{age !== null ? `${age} yrs` : '-'}</span>
                                </div>
                            </div>

                            {/* Address */}
                            <div className="flex flex-col">
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Current Address</label>
                                <textarea 
                                    className="w-full bg-gray-50 border-2 border-transparent rounded-xl px-4 py-3 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-medium text-gray-700 resize-none flex-1"
                                    rows={4} 
                                    value={formData.address} 
                                    onChange={e => setFormData({...formData, address: e.target.value})}
                                    placeholder="House No., Street, Village..."
                                />
                            </div>
                        </div>
                    </div>

                    <div className="h-px bg-gray-100"></div>

                    {/* Section 3: Financial Profile */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                            <Briefcase className="w-4 h-4" /> Membership & Financials
                        </h4>
                        
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Joined Date</label>
                                <div className="relative">
                                    <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                                    <input 
                                        type="date" 
                                        className="w-full bg-gray-50 border-2 border-transparent rounded-xl pl-10 pr-3 py-2.5 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-medium text-gray-800 text-sm"
                                        value={formData.joinedDate} 
                                        onChange={e => setFormData({...formData, joinedDate: e.target.value})} 
                                    />
                                </div>
                            </div>
                            
                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Occupation</label>
                                <input 
                                    type="text" 
                                    className="w-full bg-gray-50 border-2 border-transparent rounded-xl px-4 py-2.5 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-medium text-gray-800 text-sm"
                                    value={formData.occupation} 
                                    onChange={e => setFormData({...formData, occupation: e.target.value})}
                                    placeholder="e.g. Farmer"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-gray-700 mb-1.5">Monthly Income</label>
                                <div className="relative">
                                    <div className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 bg-emerald-100 rounded-md flex items-center justify-center text-emerald-600 font-bold text-[10px]">฿</div>
                                    <input 
                                        type="number" 
                                        className="w-full bg-gray-50 border-2 border-transparent rounded-xl pl-10 pr-4 py-2.5 outline-none focus:bg-white focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 transition-all font-bold text-gray-800 text-sm"
                                        value={formData.monthlyIncome} 
                                        onChange={e => setFormData({...formData, monthlyIncome: parseFloat(e.target.value)})}
                                        placeholder="0"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {!isEditing && (
                        <div className="flex items-start gap-4 p-4 bg-orange-50 text-orange-800 rounded-2xl border border-orange-100/50 shadow-sm">
                            <div className="p-2 bg-orange-100 rounded-full shrink-0">
                                <AlertCircle className="w-5 h-5 text-orange-600" />
                            </div>
                            <div>
                                <h5 className="font-bold text-sm mb-1">Registration Fee Required</h5>
                                <p className="text-xs text-orange-700 leading-relaxed opacity-90">
                                    A one-time application fee of <strong>฿100</strong> will be recorded upon saving. Please collect payment from the applicant.
                                </p>
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer Actions */}
                <div className="px-8 py-6 border-t border-gray-100 bg-gray-50/50 flex justify-end gap-3">
                    <button 
                        onClick={onClose} 
                        className="px-6 py-3 text-sm font-bold text-gray-600 hover:text-gray-800 hover:bg-white border border-transparent hover:border-gray-200 rounded-xl transition-all duration-200"
                    >
                        Cancel
                    </button>
                    <button 
                        onClick={onSave} 
                        disabled={idCheckStatus === 'DUPLICATE'}
                        className={`px-8 py-3 text-sm font-bold text-white rounded-xl shadow-lg shadow-emerald-500/30 flex items-center gap-2 transform active:scale-[0.98] transition-all duration-200 ${
                            idCheckStatus === 'DUPLICATE' 
                            ? 'bg-gray-300 cursor-not-allowed shadow-none' 
                            : 'bg-gradient-to-r from-emerald-600 to-emerald-500 hover:from-emerald-500 hover:to-emerald-400'
                        }`}
                    >
                        <Save className="w-4 h-4" /> 
                        {isEditing ? 'Save Changes' : 'Confirm Registration'}
                    </button>
                </div>
            </div>
        </div>
    );
};
