
import React, { useState } from 'react';
import { Member, UserRole, Transaction } from '../types';
import { UserCheck, UserX, Snowflake, Coins, Gavel, Edit2, Trash2, ChevronDown, ChevronUp, History, ArrowDownLeft, ArrowUpRight } from 'lucide-react';

interface MemberTableProps {
    members: Member[];
    transactions: Transaction[];
    userRole: UserRole;
    onOpenShare: (member: Member) => void;
    onOpenFine: (member: Member) => void;
    onOpenEdit: (member: Member) => void;
    onDelete: (member: Member) => void;
    onToggleFreeze: (member: Member) => void;
}

export const MemberTable: React.FC<MemberTableProps> = ({ 
    members, transactions, userRole, onOpenShare, onOpenFine, onOpenEdit, onDelete, onToggleFreeze 
}) => {
    const [expandedMemberId, setExpandedMemberId] = useState<string | null>(null);

    // Permission Logic based on updated requirements
    // President: View Only (No Actions)
    // Officer: Edit, Transact, Share, Freeze
    // Secretary: Edit only (assumed based on limited role, but transacting is typically Officer)
    const canModify = userRole === UserRole.OFFICER || userRole === UserRole.SECRETARY || userRole === UserRole.ADMIN;
    const canTransact = userRole === UserRole.OFFICER || userRole === UserRole.ADMIN;
    const canRecordShare = userRole === UserRole.OFFICER || userRole === UserRole.ADMIN;
    const canFreeze = userRole === UserRole.OFFICER || userRole === UserRole.ADMIN;
    const canDelete = userRole === UserRole.ADMIN || userRole === UserRole.OFFICER;

    const maskIdCard = (id: string) => {
        if (!id || id.length < 13) return id;
        return id.substring(0, 6) + '-*****-**-' + id.substring(id.length - 1);
    };

    const toggleExpand = (id: string) => {
        if (expandedMemberId === id) {
            setExpandedMemberId(null);
        } else {
            setExpandedMemberId(id);
        }
    };

    return (
        <div className="bg-white rounded-3xl shadow-sm border border-gray-200 overflow-hidden">
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-gray-50/50 border-b border-gray-200">
                  <tr>
                    <th className="p-4 w-10"></th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider whitespace-nowrap">ID</th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider whitespace-nowrap">Full Name</th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider">ID Card</th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider min-w-[150px]">Contact</th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider text-right whitespace-nowrap">Shares (฿)</th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider text-right whitespace-nowrap">Savings (฿)</th>
                    <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider">Status</th>
                    {(canModify || canTransact || canRecordShare) && <th className="p-4 text-xs font-bold uppercase text-gray-500 tracking-wider text-center">Actions</th>}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {members.map((member) => (
                    <React.Fragment key={member.id}>
                        <tr className={`transition group ${expandedMemberId === member.id ? 'bg-gray-50' : 'hover:bg-gray-50'}`}>
                        <td className="p-4">
                            <button 
                                onClick={() => toggleExpand(member.id)}
                                className={`p-1 rounded-full hover:bg-gray-200 transition ${expandedMemberId === member.id ? 'text-emerald-600 bg-emerald-50' : 'text-gray-400'}`}
                            >
                                {expandedMemberId === member.id ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                            </button>
                        </td>
                        <td className="p-4 text-sm font-mono font-medium text-gray-500 whitespace-nowrap">{member.id}</td>
                        <td className="p-4 text-sm text-gray-800 font-bold whitespace-nowrap cursor-pointer" onClick={() => toggleExpand(member.id)}>{member.fullName}</td>
                        <td className="p-4 text-sm text-gray-600 font-mono text-xs">{maskIdCard(member.idCardNumber || '')}</td>
                        <td className="p-4 text-sm text-gray-600">
                            <div className="whitespace-nowrap font-medium">{member.phoneNumber}</div>
                            <div className="text-[10px] text-gray-400 truncate max-w-[150px]">{member.address}</div>
                        </td>
                        <td className="p-4 text-sm text-purple-600 font-bold text-right whitespace-nowrap">{member.shareBalance.toLocaleString(undefined, { minimumFractionDigits: 2 })}</td>
                        <td className="p-4 text-sm text-emerald-600 font-medium text-right whitespace-nowrap">{member.savingsBalance.toLocaleString(undefined, { minimumFractionDigits: 2 })}</td>
                        <td className="p-4">
                            <div className="flex flex-col gap-1">
                                <span className={`inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold whitespace-nowrap w-fit ${
                                member.status === 'ACTIVE' ? 'bg-green-100 text-green-700 border border-green-200' : 'bg-red-100 text-red-700 border border-red-200'
                                }`}>
                                {member.status === 'ACTIVE' ? <UserCheck className="w-3 h-3 mr-1" /> : <UserX className="w-3 h-3 mr-1" />}
                                {member.status}
                                </span>
                                {member.isFrozen && (
                                    <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold whitespace-nowrap bg-blue-100 text-blue-700 border border-blue-200 w-fit">
                                        <Snowflake className="w-3 h-3 mr-1" /> Frozen
                                    </span>
                                )}
                            </div>
                        </td>
                        {(canModify || canTransact || canRecordShare) && (
                            <td className="p-4 text-center">
                                <div className="flex justify-center items-center space-x-2">
                                    {canRecordShare && (
                                        <button 
                                            onClick={() => onOpenShare(member)}
                                            className="flex items-center gap-1 px-3 py-1.5 text-purple-700 bg-purple-50 hover:bg-purple-100 rounded-lg transition border border-purple-200 text-xs font-bold whitespace-nowrap"
                                            title="Record Share Payment"
                                            disabled={member.isFrozen}
                                        >
                                            <Coins className="w-3 h-3" />
                                            <span>Add Share</span>
                                        </button>
                                    )}
                                    {canTransact && (
                                        <button onClick={() => onOpenFine(member)} className="p-2 text-orange-600 bg-orange-50 hover:bg-orange-100 rounded-lg transition border border-orange-100" title="Record Fine">
                                            <Gavel className="w-4 h-4" />
                                        </button>
                                    )}
                                    {canModify && (
                                        <button onClick={() => onOpenEdit(member)} className="p-2 text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-lg transition border border-blue-100" title="Edit Details">
                                        <Edit2 className="w-4 h-4" />
                                        </button>
                                    )}
                                    {canFreeze && (
                                        <button onClick={() => onToggleFreeze(member)} className={`p-2 rounded-lg transition border ${member.isFrozen ? 'text-gray-600 bg-gray-100 border-gray-200' : 'text-blue-600 bg-blue-50 border-blue-100'}`} title={member.isFrozen ? "Unfreeze Account" : "Freeze Account"}>
                                            <Snowflake className="w-4 h-4" />
                                        </button>
                                    )}
                                    {canDelete && (
                                        <button onClick={() => onDelete(member)} className="p-2 text-red-600 bg-red-50 hover:bg-red-100 rounded-lg transition border border-red-100" title="Close Account">
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    )}
                                </div>
                            </td>
                        )}
                        </tr>
                        {expandedMemberId === member.id && (
                            <tr className="bg-gray-50 border-b border-gray-200 animate-in fade-in slide-in-from-top-2">
                                <td colSpan={9} className="p-0">
                                    <div className="p-6 border-t border-gray-200 shadow-inner bg-gray-50/50">
                                        <div className="flex items-center gap-2 mb-4 text-gray-800">
                                            <History className="w-5 h-5 text-gray-500" />
                                            <h4 className="font-bold text-sm uppercase tracking-wide">Transaction History</h4>
                                        </div>
                                        
                                        {/* History Table */}
                                        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
                                            <table className="w-full text-left">
                                                <thead className="bg-gray-100 text-[10px] uppercase text-gray-500 font-bold">
                                                    <tr>
                                                        <th className="px-4 py-2">Date</th>
                                                        <th className="px-4 py-2">Type</th>
                                                        <th className="px-4 py-2">Description</th>
                                                        <th className="px-4 py-2 text-right">Amount</th>
                                                        <th className="px-4 py-2">Receipt</th>
                                                    </tr>
                                                </thead>
                                                <tbody className="divide-y divide-gray-100 text-xs">
                                                    {transactions.filter(t => t.memberId === member.id).length > 0 ? (
                                                        transactions.filter(t => t.memberId === member.id)
                                                        .sort((a,b) => new Date(b.date).getTime() - new Date(a.date).getTime())
                                                        .slice(0, 5) // Show last 5
                                                        .map(tx => (
                                                            <tr key={tx.id} className="hover:bg-gray-50">
                                                                <td className="px-4 py-3 font-mono text-gray-500">{tx.date}</td>
                                                                <td className="px-4 py-3">
                                                                    <span className={`px-2 py-0.5 rounded text-[9px] font-bold uppercase border ${
                                                                        tx.type.includes('DEPOSIT') || tx.type.includes('SHARE') ? 'bg-green-50 text-green-700 border-green-100' : 
                                                                        tx.type.includes('WITHDRAWAL') || tx.type.includes('EXPENSE') ? 'bg-red-50 text-red-700 border-red-100' :
                                                                        'bg-blue-50 text-blue-700 border-blue-100'
                                                                    }`}>
                                                                        {tx.type.replace('_', ' ')}
                                                                    </span>
                                                                </td>
                                                                <td className="px-4 py-3 text-gray-700">{tx.description}</td>
                                                                <td className={`px-4 py-3 text-right font-bold ${
                                                                    tx.type.includes('DEPOSIT') || tx.type.includes('SHARE') || tx.type.includes('INCOME') ? 'text-emerald-600' : 'text-red-600'
                                                                }`}>
                                                                    {tx.type.includes('DEPOSIT') || tx.type.includes('SHARE') || tx.type.includes('INCOME') ? '+' : '-'}฿{tx.amount.toLocaleString()}
                                                                </td>
                                                                <td className="px-4 py-3 font-mono text-gray-400">{tx.receiptId}</td>
                                                            </tr>
                                                        ))
                                                    ) : (
                                                        <tr><td colSpan={5} className="px-4 py-6 text-center text-gray-400">No transactions recorded.</td></tr>
                                                    )}
                                                </tbody>
                                            </table>
                                            {transactions.filter(t => t.memberId === member.id).length > 5 && (
                                                <div className="p-2 text-center border-t border-gray-100">
                                                    <span className="text-xs text-gray-400 italic">Showing recent 5 transactions</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        )}
                    </React.Fragment>
                  ))}
                  {members.length === 0 && (
                    <tr><td colSpan={9} className="p-8 text-center text-gray-500">No members found.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
        </div>
    );
};
