
import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Loan, LoanStatus, UserRole, Transaction, PaymentNotification } from '../types';
import { Plus, Loader2 } from 'lucide-react';
import ReceiptModal from './ReceiptModal';
import { ActiveLoansTable, LoanRequestsTable, OnlineVerificationTable } from './LoanTables';
import { OfficerLoanDetail, MemberLoanDetail } from './LoanDetails';
import { LoanApplicationModal, RepaymentModal, NotifyPaymentModal, MemberPaymentModal } from './LoanModals';

interface LoanManagementProps {
    userRole: UserRole;
}

const LoanManagement: React.FC<LoanManagementProps> = ({ userRole }) => {
  const [loans, setLoans] = useState<Loan[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [paymentNotifications, setPaymentNotifications] = useState<PaymentNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'ACTIVE' | 'REQUESTS' | 'VERIFY'>('ACTIVE');
  
  // Filter States
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [filterStartDate, setFilterStartDate] = useState('');
  const [filterEndDate, setFilterEndDate] = useState('');

  // Modals
  const [showApplicationModal, setShowApplicationModal] = useState(false);
  const [showRepayModal, setShowRepayModal] = useState(false); // Officer Repayment
  const [showMemberPayModal, setShowMemberPayModal] = useState(false); // Member QR Pass
  const [showNotifyModal, setShowNotifyModal] = useState(false); // Member Notify Payment
  const [selectedLoan, setSelectedLoan] = useState<Loan | null>(null);
  
  // Detail View State
  const [viewingLoan, setViewingLoan] = useState<Loan | null>(null);

  // Receipt State
  const [receiptData, setReceiptData] = useState<any>(null);
  const [showReceipt, setShowReceipt] = useState(false);
  
  // Fetch Data on Mount
  useEffect(() => {
      const fetchData = async () => {
          try {
              setLoading(true);
              const [loansData, txsData, notifsData] = await Promise.all([
                  api.loans.getAll(),
                  api.accounting.getTransactions(),
                  api.notifications.getAll()
              ]);
              
              if (userRole === UserRole.MEMBER) {
                  // Mock: Filter for current user M001
                  setLoans(loansData.filter(l => l.memberId === 'M001'));
                  setTransactions(txsData.filter(t => t.memberId === 'M001'));
                  setPaymentNotifications(notifsData.filter(n => n.memberId === 'M001'));
              } else {
                  setLoans(loansData);
                  setTransactions(txsData);
                  setPaymentNotifications(notifsData);
              }
          } catch (error) {
              console.error("Failed to fetch data", error);
          } finally {
              setLoading(false);
          }
      };
      fetchData();
  }, [userRole]);

  // Derived State with Filters
  const activeLoans = loans.filter(l => {
      // Tab Category Filter
      if (![LoanStatus.ACTIVE, LoanStatus.DEFAULTED, LoanStatus.PAID].includes(l.status)) return false;
      // UI Filters
      const matchesSearch = l.memberName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                            l.id.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesStatus = statusFilter === 'ALL' || l.status === statusFilter;
      const matchesDate = (!filterStartDate || l.startDate >= filterStartDate) && 
                          (!filterEndDate || l.startDate <= filterEndDate);
      return matchesSearch && matchesStatus && matchesDate;
  });
  
  const pendingLoans = loans.filter(l => {
       // Requests tab shows Pending and Approved (but not yet Disbursed)
       if (l.status !== LoanStatus.PENDING && l.status !== LoanStatus.APPROVED && l.status !== LoanStatus.REJECTED) return false;
       const matchesSearch = l.memberName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                             l.id.toLowerCase().includes(searchTerm.toLowerCase());
       return matchesSearch;
  });

  const pendingNotifications = paymentNotifications.filter(n => n.status === 'PENDING');

  const getLoanHistory = (loanId: string) => {
    // Simple mock filter: check if description contains Loan ID or Contract No
    return transactions.filter(t => 
        (t.description.includes(loanId) || (viewingLoan?.contractNo && t.description.includes(viewingLoan.contractNo))) 
        && t.type === 'LOAN_REPAYMENT'
    ).sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  };

  const clearFilters = () => {
      setSearchTerm('');
      setStatusFilter('ALL');
      setFilterStartDate('');
      setFilterEndDate('');
  };

  // --- Actions ---
  const handleApprove = async (id: string) => {
      await api.loans.approve(id);
      setLoans(prev => prev.map(l => l.id === id ? { ...l, status: LoanStatus.APPROVED } : l));
  };

  const handleReject = async (id: string) => {
      await api.loans.reject(id, "Admin Rejection");
      setLoans(prev => prev.map(l => l.id === id ? { ...l, status: LoanStatus.REJECTED } : l));
  };

  const handleDisburse = async (id: string) => {
      const loan = loans.find(l => l.id === id);
      if (!loan) return;
      await api.loans.disburse(id);
      
      setLoans(prev => prev.map(l => l.id === id ? { ...l, status: LoanStatus.ACTIVE, startDate: new Date().toISOString().split('T')[0] } : l));

      setReceiptData({
        receiptNo: `PAYOUT-${Date.now().toString().slice(-6)}`,
        date: new Date().toLocaleDateString(),
        receivedFrom: 'Satja Savings Group',
        description: `Loan Disbursement (ID: ${loan.id})`,
        amount: loan.principalAmount,
        cashier: userRole === UserRole.OFFICER ? 'Officer' : 'President'
      });
      setShowReceipt(true);
  };

  const handleSubmitApplication = async (form: any) => {
      const member = await api.members.getById(form.memberId || 'M001'); // Default to M001 for member view mock
      const appData = {
          memberId: userRole === UserRole.MEMBER ? 'M001' : form.memberId,
          memberName: member?.fullName || 'Unknown',
          principalAmount: parseFloat(form.principalAmount),
          remainingBalance: parseFloat(form.principalAmount),
          interestRate: 6.0,
          termMonths: parseInt(form.termMonths),
          startDate: '-', 
          status: LoanStatus.PENDING,
          loanType: form.loanType,
          guarantorIds: form.guarantorIds,
          collateralRef: form.collateralRef
      };
      
      const newLoan = await api.loans.apply(appData);
      setLoans([...loans, newLoan as Loan]);
      setShowApplicationModal(false);
      setActiveTab('REQUESTS'); 
  };

  const handleNotifyPayment = async (amount: string, file: File | null) => {
      if (!viewingLoan) return;

      const newNotif = await api.notifications.notifyPayment({
          memberId: viewingLoan.memberId,
          memberName: viewingLoan.memberName,
          loanId: viewingLoan.id,
          amount: parseFloat(amount),
          date: new Date().toISOString().split('T')[0],
          slipUrl: file ? URL.createObjectURL(file) : undefined
      });

      setPaymentNotifications([...paymentNotifications, newNotif]);
      setShowNotifyModal(false);
      alert("Payment notification submitted! An officer will verify it shortly.");
  };

  const handleApproveNotification = async (notification: PaymentNotification) => {
      // 1. Update Notification Status
      await api.notifications.approve(notification.id);
      setPaymentNotifications(prev => prev.map(n => n.id === notification.id ? { ...n, status: 'APPROVED' } : n));

      // 2. Automate Repayment Logic
      const loan = loans.find(l => l.id === notification.loanId);
      if (loan) {
          // Reuse calculation logic
          const annualRate = loan.interestRate / 100;
          const monthlyInterest = (loan.remainingBalance * annualRate) / 12;
          const accruedInterest = monthlyInterest; 
          const payInterest = Math.min(notification.amount, accruedInterest);
          const payPrincipal = Math.max(0, notification.amount - payInterest);
          const actualPrincipalPaid = Math.min(payPrincipal, loan.remainingBalance);
          const newBalance = loan.remainingBalance - actualPrincipalPaid;

          // Update Loan
          await api.loans.repay(loan.id, notification.amount);
          setLoans(prev => prev.map(l => {
              if (l.id === loan.id) {
                  return { 
                      ...l, 
                      remainingBalance: newBalance,
                      status: newBalance <= 0 ? LoanStatus.PAID : l.status 
                  };
              }
              return l;
          }));

          // 3. Create Transaction Record
          const newTx: Transaction = {
              id: `T${Date.now()}`,
              date: new Date().toISOString().split('T')[0],
              type: 'LOAN_REPAYMENT',
              amount: notification.amount,
              memberId: loan.memberId,
              description: `Online Repayment (ID: ${loan.id}) - Int: ${payInterest.toFixed(0)}, Prin: ${payPrincipal.toFixed(0)}`,
              receiptId: `REC-ONL-${Date.now().toString().slice(-6)}`
          };
          // In real app, API creates this. Here we mock update local state
          setTransactions(prev => [newTx, ...prev]);

          // 4. Generate Receipt
          setReceiptData({
            receiptNo: newTx.receiptId,
            date: new Date().toLocaleDateString(),
            receivedFrom: loan.memberName,
            description: `Online Loan Payment (Verified)`,
            amount: notification.amount,
            cashier: 'System (Online)'
          });
          setShowReceipt(true);
      }
  };

  const handleSubmitRepayment = async (amountStr: string, breakdown: any) => {
      if (selectedLoan && amountStr) {
          const amount = parseFloat(amountStr);
          await api.loans.repay(selectedLoan.id, amount);
          
          setLoans(prev => prev.map(l => {
              if (l.id === selectedLoan.id) {
                  return { 
                      ...l, 
                      remainingBalance: breakdown.newBalance,
                      status: breakdown.newBalance <= 0 ? LoanStatus.PAID : l.status 
                  };
              }
              return l;
          }));

          // Add transaction to local state for immediate UI update
          const newTx: Transaction = {
              id: `T${Date.now()}`,
              date: new Date().toISOString().split('T')[0],
              type: 'LOAN_REPAYMENT',
              amount: amount,
              memberId: selectedLoan.memberId,
              description: `Repayment (ID: ${selectedLoan.id}) - Int: ${breakdown.interest.toFixed(0)}, Prin: ${breakdown.principal.toFixed(0)}`,
              receiptId: `REC-${Date.now().toString().slice(-6)}`
          };
          setTransactions(prev => [newTx, ...prev]);
          
          setReceiptData({
            receiptNo: newTx.receiptId,
            date: new Date().toLocaleDateString(),
            receivedFrom: selectedLoan.memberName,
            description: `Loan Installment (P: ${breakdown.principal.toLocaleString()} / I: ${breakdown.interest.toLocaleString()})`,
            amount: amount,
            cashier: userRole === UserRole.PRESIDENT ? 'President' : 'Officer'
          });

          setShowRepayModal(false);
          setSelectedLoan(null);
          setShowReceipt(true);
      }
  };

  // Permissions
  const canApprove = userRole === UserRole.PRESIDENT || userRole === UserRole.SECRETARY;
  const canApply = userRole === UserRole.OFFICER || userRole === UserRole.SECRETARY || userRole === UserRole.PRESIDENT || userRole === UserRole.MEMBER;
  const canRepay = userRole === UserRole.OFFICER || userRole === UserRole.PRESIDENT;
  const canDisburse = userRole === UserRole.OFFICER || userRole === UserRole.PRESIDENT;
  const canVerifyOnline = userRole === UserRole.OFFICER;

  if (loading) {
       return <div className="flex h-96 items-center justify-center"><Loader2 className="w-10 h-10 text-emerald-600 animate-spin" /></div>;
  }

  // --- OFFICER/ADMIN DETAIL VIEW ---
  if (viewingLoan && userRole !== UserRole.MEMBER) {
      return (
          <OfficerLoanDetail 
            loan={viewingLoan}
            onBack={() => setViewingLoan(null)}
            onRepay={(loan) => { setSelectedLoan(loan); setShowRepayModal(true); }}
            onDisburse={handleDisburse}
            canRepay={canRepay}
            canDisburse={canDisburse}
          />
      )
  }

  // --- MEMBER VIEW: DIGITAL PASSBOOK (Detail) ---
  if (viewingLoan && userRole === UserRole.MEMBER) {
      return (
          <MemberLoanDetail 
             loan={viewingLoan}
             history={getLoanHistory(viewingLoan.id)}
             onBack={() => setViewingLoan(null)}
             onNotifyPayment={() => setShowNotifyModal(true)}
             onShowPaymentModal={() => setShowMemberPayModal(true)}
             onShowReceipt={(data) => { setReceiptData(data); setShowReceipt(true); }}
          />
      );
  }

  // --- View: OFFICER (Management Table) ---
  return (
    <div className="space-y-6 pb-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
            <h2 className="text-2xl font-bold text-gray-800">
                Loans & Credits
            </h2>
            <p className="text-sm text-gray-500">
                 Manage Lending & Approvals
            </p>
        </div>
        {canApply && (
            <button 
                onClick={() => setShowApplicationModal(true)}
                className="w-full sm:w-auto bg-emerald-600 text-white px-5 py-2.5 rounded-xl hover:bg-emerald-700 transition flex items-center justify-center space-x-2 shadow-lg shadow-emerald-200/50 active:scale-95 duration-150 font-bold text-sm"
            >
                <Plus className="w-5 h-5" />
                <span>New Application</span>
            </button>
        )}
      </div>

      <div className="bg-white rounded-3xl shadow-sm border border-gray-200 overflow-hidden">
          {/* Tabs */}
          <div className="flex border-b border-gray-200 overflow-x-auto no-scrollbar">
              <button 
                onClick={() => { setActiveTab('ACTIVE'); clearFilters(); }}
                className={`flex-1 md:flex-none px-6 py-4 text-sm font-bold border-b-2 transition whitespace-nowrap ${
                    activeTab === 'ACTIVE' 
                    ? 'border-emerald-600 text-emerald-600 bg-emerald-50/50' 
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                }`}
              >
                Active Loans & Repayments
              </button>
              <button 
                onClick={() => { setActiveTab('REQUESTS'); clearFilters(); }}
                className={`flex-1 md:flex-none px-6 py-4 text-sm font-bold border-b-2 transition flex items-center justify-center whitespace-nowrap ${
                    activeTab === 'REQUESTS' 
                    ? 'border-emerald-600 text-emerald-600 bg-emerald-50/50' 
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                }`}
              >
                Approval Requests
                {loans.filter(l => l.status === LoanStatus.PENDING).length > 0 && (
                    <span className="ml-2 bg-yellow-100 text-yellow-800 text-xs px-2 py-0.5 rounded-full">
                        {loans.filter(l => l.status === LoanStatus.PENDING).length}
                    </span>
                )}
              </button>
              {canVerifyOnline && (
                  <button 
                    onClick={() => { setActiveTab('VERIFY'); clearFilters(); }}
                    className={`flex-1 md:flex-none px-6 py-4 text-sm font-bold border-b-2 transition flex items-center justify-center whitespace-nowrap ${
                        activeTab === 'VERIFY' 
                        ? 'border-emerald-600 text-emerald-600 bg-emerald-50/50' 
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    Online Verification
                    {pendingNotifications.length > 0 && (
                        <span className="ml-2 bg-red-100 text-red-800 text-xs px-2 py-0.5 rounded-full">
                            {pendingNotifications.length}
                        </span>
                    )}
                  </button>
              )}
          </div>

          {activeTab === 'ACTIVE' && (
              <ActiveLoansTable 
                  loans={activeLoans}
                  onView={setViewingLoan}
                  onRepay={(loan) => { setSelectedLoan(loan); setShowRepayModal(true); }}
                  onDisburse={handleDisburse}
                  canRepay={canRepay}
                  canDisburse={canDisburse}
              />
          )}

          {activeTab === 'REQUESTS' && (
              <LoanRequestsTable 
                  loans={pendingLoans}
                  onApprove={handleApprove}
                  onReject={handleReject}
                  onDisburse={handleDisburse}
                  canApprove={canApprove}
                  canDisburse={canDisburse}
              />
          )}

          {activeTab === 'VERIFY' && canVerifyOnline && (
              <OnlineVerificationTable 
                  notifications={pendingNotifications}
                  onVerify={handleApproveNotification}
              />
          )}
      </div>

      {/* MODALS */}
      <LoanApplicationModal 
          isOpen={showApplicationModal}
          onClose={() => setShowApplicationModal(false)}
          onSubmit={handleSubmitApplication}
          userRole={userRole}
          defaultMemberId={userRole === UserRole.MEMBER ? 'M001' : ''}
      />

      <NotifyPaymentModal 
          isOpen={showNotifyModal}
          onClose={() => setShowNotifyModal(false)}
          onSubmit={handleNotifyPayment}
      />
      
      {selectedLoan && (
          <RepaymentModal 
              isOpen={showRepayModal}
              onClose={() => setShowRepayModal(false)}
              loan={selectedLoan}
              onSubmit={handleSubmitRepayment}
          />
      )}

      {viewingLoan && (
          <MemberPaymentModal 
              isOpen={showMemberPayModal}
              onClose={() => setShowMemberPayModal(false)}
              loan={viewingLoan}
          />
      )}

      <ReceiptModal isOpen={showReceipt} onClose={() => setShowReceipt(false)} data={receiptData} />
    </div>
  );
};

export default LoanManagement;
