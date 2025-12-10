import React, { useState } from 'react';
import { Member, Transaction } from '../types';
import { ShieldCheck, QrCode, Share2, User, CreditCard, Calendar, Briefcase, Wallet, Phone, MapPin, History, ArrowDownLeft, ArrowUpRight } from 'lucide-react';

interface MemberProfileProps {
    member: Member;
    transactions?: Transaction[];
}

export const MemberProfile: React.FC<MemberProfileProps> = ({ member, transactions = [] }) => {
    const [isFlipped, setIsFlipped] = useState(false);
    const totalWealth = member.shareBalance + member.savingsBalance;

    const maskIdCard = (id: string) => {
        if (!id || id.length < 13) return id;
        return id.substring(0, 6) + '-*****-**-' + id.substring(id.length - 1);
    };

    const calculateAge = (birthDateString: string) => {
        if (!birthDateString) return 'N/A';
        const today = new Date();
        const birthDate = new Date(birthDateString);
        let age = today.getFullYear() - birthDate.getFullYear();
        const m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    };

    const InfoItem: React.FC<{ icon: any, label: string, value: string, subValue?: string, mono?: boolean, fullWidth?: boolean, action?: boolean }> = ({ icon: Icon, label, value, subValue, mono = false, fullWidth = false, action = false }) => (
        <div className={`${fullWidth ? 'md:col-span-2' : ''} group`}>
            <div className="flex items-center gap-2 mb-2">
                <Icon className="w-4 h-4 text-gray-400 group-hover:text-emerald-600 transition-colors" />
                <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">{label}</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-xl border border-gray-100 group-hover:border-emerald-200 group-hover:bg-emerald-50/30 transition-all shadow-sm group-hover:shadow-md">
                <div className="flex flex-col">
                    <span className={`text-gray-900 font-medium ${mono ? 'font-mono tracking-wide' : ''}`}>{value}</span>
                    {subValue && <span className="text-[10px] text-gray-500">{subValue}</span>}
                </div>
                {action && (
                    <button className="p-1.5 text-gray-400 hover:text-emerald-600 hover:bg-emerald-100 rounded-lg transition" title="Copy">
                        <Share2 className="w-3.5 h-3.5" />
                    </button>
                )}
            </div>
        </div>
    );

    return (
        <div className="space-y-8 animate-fade-in pb-12 max-w-7xl mx-auto">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Left Column: Digital Identity Card */}
                <div className="lg:col-span-1 space-y-6">
                    {/* The Card (Flip Container) - Constrained width on mobile/tablet */}
                    <div className="relative w-full max-w-md lg:max-w-none mx-auto aspect-[1.586/1] perspective-1000 group cursor-pointer" onClick={() => setIsFlipped(!isFlipped)}>
                        <div className={`relative w-full h-full transition-all duration-700 [transform-style:preserve-3d] ${isFlipped ? '[transform:rotateY(180deg)]' : ''}`}>
                            
                            {/* Front Face */}
                            <div className="absolute inset-0 w-full h-full [backface-visibility:hidden]">
                                <div className="bg-gradient-to-bl from-emerald-900 via-emerald-800 to-teal-900 rounded-3xl p-8 text-white shadow-2xl relative overflow-hidden h-full border border-emerald-700/50">
                                    {/* Background Pattern */}
                                    <div className="absolute inset-0 opacity-10" style={{ backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)', backgroundSize: '24px 24px' }}></div>
                                    <div className="absolute top-0 right-0 w-64 h-64 bg-emerald-500/20 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
                                    <div className="absolute bottom-0 left-0 w-48 h-48 bg-teal-500/20 rounded-full blur-3xl -ml-12 -mb-12 pointer-events-none"></div>

                                    <div className="relative z-10 h-full flex flex-col justify-between">
                                        <div className="flex justify-between items-start">
                                            <div className="flex items-center gap-3">
                                                <div className="p-2 bg-white/10 backdrop-blur-md rounded-xl border border-white/20">
                                                    <ShieldCheck className="w-6 h-6 text-emerald-300" />
                                                </div>
                                                <div>
                                                    <h3 className="font-bold text-lg leading-none">Satja Savings</h3>
                                                    <p className="text-[10px] text-emerald-300/80 font-medium tracking-wider uppercase mt-1">Official Member</p>
                                                </div>
                                            </div>
                                            {/* Click hint/icon */}
                                            <div className="relative group/qr">
                                                <QrCode className="w-12 h-12 text-white/80 mix-blend-overlay transition-transform group-hover/qr:scale-110" />
                                                <div className="absolute -bottom-6 left-1/2 -translate-x-1/2 text-[9px] text-white/60 whitespace-nowrap opacity-0 group-hover/qr:opacity-100 transition-opacity">
                                                    Tap to flip
                                                </div>
                                            </div>
                                        </div>

                                        <div className="space-y-4">
                                            <div className="flex items-end gap-4">
                                                <div className="flex-1">
                                                    <p className="text-[10px] text-emerald-300 uppercase font-bold tracking-widest mb-1">Member Name</p>
                                                    <p className="text-xl md:text-2xl font-bold text-white tracking-wide truncate">{member.fullName}</p>
                                                </div>
                                                <div className="shrink-0 text-right">
                                                    <p className="text-[10px] text-emerald-300 uppercase font-bold tracking-widest mb-1">Status</p>
                                                    <span className={`inline-flex items-center px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase border ${member.status === 'ACTIVE' ? 'bg-emerald-500/20 border-emerald-400/30 text-emerald-100' : 'bg-red-500/20 border-red-400/30 text-red-100'}`}>
                                                        {member.status}
                                                    </span>
                                                </div>
                                            </div>
                                            
                                            <div className="flex justify-between items-end pt-4 border-t border-white/10">
                                                <div>
                                                    <p className="text-[9px] text-emerald-400 uppercase font-bold tracking-widest mb-0.5">Member ID</p>
                                                    <p className="font-mono text-lg text-white/90 tracking-wider">{member.id}</p>
                                                </div>
                                                <div className="text-right">
                                                    <p className="text-[9px] text-emerald-400 uppercase font-bold tracking-widest mb-0.5">Joined</p>
                                                    <p className="font-medium text-sm text-white/90">{member.joinedDate}</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Back Face */}
                            <div className="absolute inset-0 w-full h-full [backface-visibility:hidden] [transform:rotateY(180deg)]">
                                <div className="bg-gray-800 rounded-3xl h-full shadow-2xl relative overflow-hidden flex flex-col justify-between border border-gray-700">
                                    {/* Magnetic Strip */}
                                    <div className="w-full h-12 bg-black mt-8 opacity-90"></div>

                                    <div className="px-8 pb-8 flex-1 flex flex-col justify-between pt-4">
                                        <div className="flex justify-between items-start gap-4">
                                            <div className="flex-1">
                                                {/* Signature Strip */}
                                                <div className="bg-white/90 h-10 w-full mb-3 flex items-center justify-start px-2 relative pattern-dots">
                                                    <div className="text-gray-400 font-handwriting text-lg italic opacity-70 ml-2" style={{ fontFamily: 'cursive' }}>
                                                        {member.fullName}
                                                    </div>
                                                    <div className="absolute right-2 top-1/2 -translate-x-1/2 text-[8px] text-gray-500 font-mono">
                                                        AUTHORIZED SIGNATURE
                                                    </div>
                                                </div>
                                                <p className="text-[9px] text-gray-400 leading-tight">
                                                    This card is non-transferable and remains the property of Satja Savings Ban Sai Yai Group. 
                                                    Please return if found.
                                                </p>
                                            </div>
                                            <div className="bg-white p-1.5 rounded-xl shrink-0 shadow-lg">
                                                <QrCode className="w-20 h-20 text-black" />
                                            </div>
                                        </div>

                                        <div className="flex justify-between items-end border-t border-gray-700 pt-4">
                                            <div>
                                                <p className="text-[9px] text-gray-500 uppercase font-bold mb-1">Support Contact</p>
                                                <p className="text-xs font-mono text-gray-300">089-123-4567</p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-[9px] text-gray-500 uppercase font-bold mb-1">Security Code</p>
                                                <p className="font-mono text-lg text-emerald-400 tracking-widest">
                                                    {member.id.replace('M', '9').padStart(4, '0')}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>

                    {/* Financial Snapshot (Mini) */}
                    <div className="bg-white rounded-3xl p-6 shadow-sm border border-gray-100 max-w-md lg:max-w-none mx-auto w-full">
                        <h4 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-4">Total Net Worth</h4>
                        <div className="flex items-baseline gap-1 mb-2">
                             <span className="text-3xl font-bold text-gray-900">฿{totalWealth.toLocaleString()}</span>
                             <span className="text-sm font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">+12% YTD</span>
                        </div>
                        <div className="w-full bg-gray-100 rounded-full h-2 mb-4 overflow-hidden flex">
                             <div className="bg-purple-500 h-full" style={{ width: `${(member.shareBalance / totalWealth) * 100}%` }}></div>
                             <div className="bg-emerald-500 h-full" style={{ width: `${(member.savingsBalance / totalWealth) * 100}%` }}></div>
                        </div>
                        <div className="flex justify-between text-xs font-medium text-gray-500">
                            <div className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-purple-500"></div> Shares</div>
                            <div className="flex items-center gap-1.5"><div className="w-2 h-2 rounded-full bg-emerald-500"></div> Savings</div>
                        </div>
                    </div>
                </div>

                {/* Right Column: Detailed Info Grid */}
                <div className="lg:col-span-2 space-y-6">
                     {/* Info Groups */}
                     <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
                          <div className="p-6 border-b border-gray-50 bg-gray-50/50 flex items-center gap-3">
                               <div className="p-2 bg-blue-50 text-blue-600 rounded-xl">
                                   <User className="w-5 h-5" />
                               </div>
                               <h3 className="font-bold text-gray-800 text-lg">Personal Details</h3>
                          </div>
                          <div className="p-6 md:p-8 grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-8">
                                <InfoItem icon={CreditCard} label="Civil ID Number" value={maskIdCard(member.idCardNumber || '')} mono />
                                <InfoItem icon={Calendar} label="Date of Birth" value={member.birthDate} subValue={`Age: ${calculateAge(member.birthDate)} Years`} />
                                <InfoItem icon={Briefcase} label="Occupation" value={member.occupation || 'Not Specified'} />
                                <InfoItem icon={Wallet} label="Monthly Income" value={`฿${(member.monthlyIncome || 0).toLocaleString()}`} />
                          </div>
                     </div>

                     <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
                          <div className="p-6 border-b border-gray-50 bg-gray-50/50 flex items-center gap-3">
                               <div className="p-2 bg-orange-50 text-orange-600 rounded-xl">
                                   <MapPin className="w-5 h-5" />
                               </div>
                               <h3 className="font-bold text-gray-800 text-lg">Contact Information</h3>
                          </div>
                          <div className="p-6 md:p-8 grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-8">
                                <InfoItem icon={Phone} label="Phone Number" value={member.phoneNumber} action />
                                <InfoItem icon={MapPin} label="Home Address" value={member.address} fullWidth />
                          </div>
                     </div>

                     {/* Transaction History Section */}
                     <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
                        <div className="p-6 border-b border-gray-50 bg-gray-50/50 flex items-center gap-3">
                            <div className="p-2 bg-purple-50 text-purple-600 rounded-xl">
                                <History className="w-5 h-5" />
                            </div>
                            <h3 className="font-bold text-gray-800 text-lg">Recent Transactions</h3>
                        </div>
                        <div className="p-6">
                            {transactions.length > 0 ? (
                                <div className="space-y-4">
                                    {transactions
                                        .sort((a,b) => new Date(b.date).getTime() - new Date(a.date).getTime())
                                        .slice(0, 5)
                                        .map(t => (
                                        <div key={t.id} className="flex items-center justify-between p-3 border border-gray-100 rounded-xl hover:bg-gray-50 transition">
                                            <div className="flex items-center gap-4">
                                                <div className={`p-2 rounded-full ${
                                                    t.type.includes('DEPOSIT') || t.type.includes('SHARE') ? 'bg-emerald-100 text-emerald-600' : 'bg-red-100 text-red-600'
                                                }`}>
                                                    {t.type.includes('DEPOSIT') || t.type.includes('SHARE') ? <ArrowDownLeft className="w-4 h-4" /> : <ArrowUpRight className="w-4 h-4" />}
                                                </div>
                                                <div>
                                                    <p className="font-bold text-gray-800 text-sm">{t.description}</p>
                                                    <p className="text-xs text-gray-500">{t.date} • {t.type.replace('_', ' ')}</p>
                                                </div>
                                            </div>
                                            <div className={`font-bold ${
                                                t.type.includes('DEPOSIT') || t.type.includes('SHARE') ? 'text-emerald-600' : 'text-red-600'
                                            }`}>
                                                {t.type.includes('DEPOSIT') || t.type.includes('SHARE') ? '+' : '-'}฿{t.amount.toLocaleString()}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="text-center text-gray-400 py-4 text-sm">No transaction history found.</p>
                            )}
                        </div>
                     </div>
                </div>
            </div>
        </div>
    );
};
