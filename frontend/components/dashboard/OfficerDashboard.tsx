
import React, { useState, useEffect } from 'react';
import { Wallet, ArrowDownLeft, ArrowUpRight, Users, Search, Loader2, Monitor } from 'lucide-react';
import StatCard from './StatCard';
import { ViewState, Transaction, Member, Loan } from '../../types';
import { api } from '../../services/api';
import { OfficerQuickActions, OfficerTransactions, OfficerPendingTasks } from './OfficerWidgets';
import { PaymentTerminal } from '../OfficerTools';
import ReceiptModal from '../ReceiptModal';

interface OfficerDashboardProps {
    onNavigate: (view: ViewState) => void;
}

const OfficerDashboard: React.FC<OfficerDashboardProps> = ({ onNavigate }) => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
      totalMembers: 0,
      activeMembers: 0,
      cashBalance: 0,
      depositsToday: 0,
      repaymentsToday: 0,
      pendingTasks: 0
  });
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>([]);
  const [members, setMembers] = useState<Member[]>([]);
  const [loans, setLoans] = useState<Loan[]>([]);

  // Terminal & Receipt State
  const [showTerminal, setShowTerminal] = useState(false);
  const [receiptData, setReceiptData] = useState<any>(null);
  const [showReceipt, setShowReceipt] = useState(false);

  useEffect(() => {
    const loadDashboardData = async () => {
        try {
            setLoading(true);
            const [membersData, loansData, officerStats, transactions] = await Promise.all([
                api.members.getAll(),
                api.loans.getAll(),
                api.dashboard.getOfficerStats(),
                api.accounting.getTransactions()
            ]);

            setMembers(membersData);
            setLoans(loansData);

            setStats({
                totalMembers: membersData.length,
                activeMembers: membersData.filter(m => m.status === 'ACTIVE').length,
                cashBalance: officerStats.cashBalance,
                depositsToday: officerStats.depositsToday,
                repaymentsToday: officerStats.repaymentsToday,
                pendingTasks: officerStats.pendingTasks
            });

            setRecentTransactions(transactions.slice(0, 5));
        } catch (error) {
            console.error("Failed to load officer dashboard", error);
        } finally {
            setLoading(false);
        }
    };
    loadDashboardData();
  }, []);

  const handleTerminalPayment = async (data: any) => {
      // 1. Process Logic (Mock API Calls)
      if (data.type === 'SHARE_PURCHASE') {
          await api.savings.deposit(data.memberId, data.amount);
      } else if (data.type === 'LOAN_REPAYMENT') {
          await api.loans.repay(data.loanId, data.amount);
      }

      // 2. Create local Transaction record for UI update
      const newTx: Transaction = {
          id: `T${Date.now()}`,
          date: new Date().toISOString().split('T')[0],
          type: data.type,
          amount: data.amount,
          memberId: data.memberId,
          description: data.description,
          receiptId: `REC-${Date.now().toString().slice(-6)}`
      };
      setRecentTransactions(prev => [newTx, ...prev]);

      // 3. Update Stats (Optimistic)
      setStats(prev => ({
          ...prev,
          cashBalance: prev.cashBalance + data.amount,
          depositsToday: data.type === 'SHARE_PURCHASE' ? prev.depositsToday + 1 : prev.depositsToday,
          repaymentsToday: data.type === 'LOAN_REPAYMENT' ? prev.repaymentsToday + 1 : prev.repaymentsToday
      }));

      // 4. Show Receipt
      const memberName = members.find(m => m.id === data.memberId)?.fullName || 'Unknown';
      setReceiptData({
          receiptNo: newTx.receiptId,
          date: new Date().toLocaleDateString(),
          receivedFrom: memberName,
          description: data.description,
          amount: data.amount,
          cashier: 'Officer (Terminal)'
      });
      setShowReceipt(true);
  };

  const pendingTasksList = [
    { id: 1, task: 'Verify new member application: Somsak J.', status: 'Pending', type: 'PENDING' },
    { id: 2, task: 'Approve share withdrawal: M003', status: 'Urgent', type: 'URGENT' },
  ];

  if (loading) {
      return <div className="flex h-96 items-center justify-center"><Loader2 className="w-10 h-10 text-emerald-600 animate-spin" /></div>;
  }

  return (
    <div className="space-y-8 animate-fade-in pb-8">
      {/* Header with Search */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">Officer Dashboard</h2>
          <p className="text-sm text-gray-500 font-medium mt-1">Daily Operations & Transaction Management</p>
        </div>
        
        {/* Terminal Trigger */}
        <button 
            onClick={() => setShowTerminal(true)}
            className="w-full md:w-auto bg-gray-900 text-white px-6 py-3 rounded-2xl font-bold shadow-lg shadow-gray-300 hover:bg-black transition flex items-center justify-center gap-3 group"
        >
            <Monitor className="w-5 h-5 text-emerald-400 group-hover:scale-110 transition-transform" />
            <span>Open Payment Terminal</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6">
         {/* Featured Card */}
         <div className="bg-[#00b074] rounded-3xl p-6 text-white shadow-xl shadow-emerald-200/50 relative overflow-hidden group">
             <div className="absolute top-0 right-0 p-6 opacity-10 transform translate-x-4 -translate-y-4 group-hover:scale-110 transition-transform duration-500">
                 <Users className="w-24 h-24" />
             </div>
             <div className="relative z-10 h-full flex flex-col justify-between min-h-[140px]">
                 <div>
                     <p className="text-emerald-50 font-medium mb-1">Total Members</p>
                     <h3 className="text-4xl font-bold">{stats.totalMembers}</h3>
                 </div>
                 <div>
                     <span className="bg-white/20 backdrop-blur-md text-white text-xs font-bold px-3 py-1.5 rounded-lg inline-flex items-center">
                         {stats.activeMembers} Active members
                     </span>
                 </div>
             </div>
         </div>

         <StatCard
           title="Cash Balance"
           value={`฿${stats.cashBalance.toLocaleString()}`}
           icon={Wallet}
           colorClass="bg-blue-50 text-blue-600"
           subtext="Current cash on hand"
         />
         <StatCard
           title="Deposits Today"
           value={`฿${stats.depositsToday.toLocaleString()}`}
           icon={ArrowDownLeft}
           colorClass="bg-emerald-50 text-emerald-600"
           subtext="12 transactions"
         />
         <StatCard
           title="Repayments Today"
           value={`฿${stats.repaymentsToday.toLocaleString()}`}
           icon={ArrowUpRight}
           colorClass="bg-indigo-50 text-indigo-600"
           subtext="5 transactions"
         />
      </div>

      {/* Quick Actions Widget */}
      <OfficerQuickActions onNavigate={onNavigate} />

      {/* Bottom Section: Transactions & Tasks */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2">
              <OfficerTransactions transactions={recentTransactions} />
          </div>
          <div>
              <OfficerPendingTasks tasks={pendingTasksList} />
          </div>
      </div>

      {/* Payment Terminal Modal */}
      <PaymentTerminal 
          isOpen={showTerminal}
          onClose={() => setShowTerminal(false)}
          members={members}
          loans={loans}
          onProcessPayment={handleTerminalPayment}
      />

      <ReceiptModal 
          isOpen={showReceipt} 
          onClose={() => setShowReceipt(false)} 
          data={receiptData} 
      />
    </div>
  );
};

export default OfficerDashboard;
