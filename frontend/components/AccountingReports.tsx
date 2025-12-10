
import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Account, UserRole, Transaction } from '../types';
import { MOCK_MEMBERS, MOCK_LOANS } from '../constants'; // Keeping for aggregations not yet in API
import { Printer, Loader2, Filter, Building2, FileSpreadsheet } from 'lucide-react';
import ReceiptModal from './ReceiptModal';
import { FinancialStatements } from './FinancialStatements';
import { JournalReport } from './accounting/JournalReport';
import { DividendManager } from './accounting/DividendManager';
import { AccountingSettings } from './accounting/AccountingSettings';
import { TransactionModal } from './accounting/TransactionModal';
import { AccountModal } from './accounting/AccountModal';
import { exportToExcel } from '../services/excelService';

interface AccountingReportsProps {
    userRole: UserRole;
}

const AccountingReports: React.FC<AccountingReportsProps> = ({ userRole }) => {
  // Officer defaults to JOURNAL; others to STATEMENTS
  const [activeTab, setActiveTab] = useState<'STATEMENTS' | 'JOURNAL' | 'DIVIDENDS' | 'SETTINGS'>(
      userRole === UserRole.OFFICER ? 'JOURNAL' : 'STATEMENTS'
  );
  
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);

  // --- Report Date Filters (Global) ---
  const [reportStartDate, setReportStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [reportEndDate, setReportEndDate] = useState(new Date().toISOString().split('T')[0]);

  // --- Modal States ---
  const [showTransactionModal, setShowTransactionModal] = useState(false);
  const [showAddAccountModal, setShowAddAccountModal] = useState(false);
  const [showReceipt, setShowReceipt] = useState(false);
  const [receiptData, setReceiptData] = useState<any>(null);
  const [receiptType, setReceiptType] = useState<'RECEIPT' | 'REPORT'>('RECEIPT');

  useEffect(() => {
      const fetchData = async () => {
          setLoading(true);
          try {
              const [txs, accs] = await Promise.all([
                  api.accounting.getTransactions(),
                  api.accounting.getAccounts()
              ]);
              setTransactions(txs);
              setAccounts(accs);
          } catch (e) {
              console.error(e);
          } finally {
              setLoading(false);
          }
      };
      fetchData();
  }, []);

  // --- Derived Financial Data (Using fetched data) ---
  const totalSavings = MOCK_MEMBERS.reduce((acc, m) => acc + m.savingsBalance, 0);
  const totalShares = MOCK_MEMBERS.reduce((acc, m) => acc + m.shareBalance, 0);
  const totalLoansRemaining = MOCK_LOANS.reduce((acc, l) => acc + l.remainingBalance, 0);
  
  // Breakdown Data for Display
  const cashAssets = accounts.filter(a => a.category === 'ASSET');
  const totalCashAssets = cashAssets.reduce((sum, a) => sum + a.balance, 0);
  
  const otherLiabilities = accounts.filter(a => a.category === 'LIABILITY');
  const totalOtherLiabilities = otherLiabilities.reduce((sum, a) => sum + a.balance, 0);

  const otherEquity = accounts.filter(a => a.category === 'EQUITY');
  const totalOtherEquity = otherEquity.reduce((sum, a) => sum + a.balance, 0);

  // Balance Sheet Aggregation
  const assets = totalCashAssets + totalLoansRemaining; 
  const liabilities = totalOtherLiabilities + totalSavings; 
  const equityBase = totalOtherEquity + totalShares;
  
  // Profit Calculation
  const revenueAccounts = accounts.filter(a => a.category === 'REVENUE');
  const revenue = revenueAccounts.reduce((acc, a) => acc + a.balance, 0);
  
  const expenseAccounts = accounts.filter(a => a.category === 'EXPENSE');
  const expenses = expenseAccounts.reduce((acc, a) => acc + a.balance, 0);
  
  const netProfit = revenue - expenses;

  // Final Equity (Including Current Year Profit)
  const totalEquity = equityBase + netProfit;

  // Chart Data
  const assetCompositionData = [
    { name: 'Cash & Bank', value: totalCashAssets },
    { name: 'Loans Receivable', value: totalLoansRemaining },
  ];
  const performanceData = [
      { name: 'Current Period', income: revenue, expense: expenses }
  ];

  // Permissions
  const canRecordJournal = userRole === UserRole.OFFICER || userRole === UserRole.SECRETARY || userRole === UserRole.PRESIDENT;
  const canExecDividend = userRole === UserRole.SECRETARY;
  const canExport = userRole === UserRole.SECRETARY || userRole === UserRole.PRESIDENT;
  const canEditDividendRate = userRole === UserRole.SECRETARY || userRole === UserRole.PRESIDENT;
  const canManageAccounts = userRole === UserRole.SECRETARY;

  // --- Actions ---
  const handleSaveTransaction = async (newTrans: Partial<Transaction>) => {
      const created = await api.accounting.createEntry(newTrans);

      // Optimistic update of Account Balance
      setAccounts(prev => prev.map(a => {
          if (a.name === newTrans.category) {
              // Note: Ideally match by code, but simplified here for mock
              return { ...a, balance: a.balance + (newTrans.amount || 0) };
          }
          return a;
      }));
      
      setTransactions([created as Transaction, ...transactions]);
      setShowTransactionModal(false);
  };

  const handleAddAccount = (newAccount: Partial<Account>) => {
      // In real app, this would be an API call
      setAccounts([...accounts, { ...newAccount, balance: 0 } as Account]);
      setShowAddAccountModal(false);
  };

  const handleDeleteAccount = (code: string) => {
      if (confirm('Are you sure you want to delete this account?')) {
          setAccounts(prev => prev.filter(a => a.code !== code));
      }
  };

  const handlePrintReceipt = (t: Transaction) => {
      setReceiptData({
          receiptNo: t.receiptId || 'N/A',
          date: t.date,
          receivedFrom: t.memberId ? MOCK_MEMBERS.find(m => m.id === t.memberId)?.fullName : 'General Fund',
          description: t.description,
          amount: t.amount,
          cashier: 'Officer (History)'
      });
      setReceiptType('RECEIPT');
      setShowReceipt(true);
  };

  const handlePrintClosingReport = (simulationData: any) => {
      if (!simulationData) return;
      setReceiptData({
          date: new Date().toLocaleDateString(),
          period: 'October 2023',
          items: [
              { label: 'Active Loans Count', value: simulationData.activeLoans },
              { label: 'Total Income (Revenue)', value: simulationData.totalIncome },
              { label: 'Total Expenses', value: simulationData.totalExpense },
              { label: 'Net Balance (Profit/Loss)', value: simulationData.netBalance },
              { label: 'Grand Total Forward (B/F)', value: simulationData.broughtForward, isTotal: true },
          ]
      });
      setReceiptType('REPORT');
      setShowReceipt(true);
  };

  const handleExcelExport = () => {
      const period = { start: reportStartDate, end: reportEndDate };
      
      if (activeTab === 'STATEMENTS') {
          // Prepare Balance Sheet Data
          const balanceSheetData = {
              assets, liabilities, equity: totalEquity, accounts
          };
          exportToExcel('BALANCE_SHEET', balanceSheetData, period);
      } else if (activeTab === 'JOURNAL') {
          // Prepare Journal Data (Filtered by date in real app, simplified here)
          exportToExcel('INCOME_EXPENSE', transactions, period);
      } else if (activeTab === 'DIVIDENDS') {
          // Mock Dividend Data Preparation
          const dividendRate = 4.5;
          const avgReturnRate = 12.0;
          const memberData = MOCK_MEMBERS.map(m => {
              // Mock calculation logic repeated from DividendManager
              const interestPaid = 500; // Mock fixed
              const divAmt = m.shareBalance * (dividendRate/100);
              const retAmt = interestPaid * (avgReturnRate/100);
              return {
                  id: m.id,
                  fullName: m.fullName,
                  shareBalance: m.shareBalance,
                  dividendAmount: divAmt,
                  interestPaid: interestPaid,
                  returnAmount: retAmt,
                  totalPayout: divAmt + retAmt
              };
          });
          exportToExcel('DIVIDEND', { members: memberData, dividendRate, avgReturnRate }, period);
      } else {
          alert('Excel export not available for this tab.');
      }
  };

  if (loading) {
       return <div className="flex h-96 items-center justify-center"><Loader2 className="w-10 h-10 text-emerald-600 animate-spin" /></div>;
  }

  return (
    <div className="space-y-8 pb-8">
      {/* Print Only Header - GOVERNMENT STYLE */}
      <div className="hidden print:block mb-8 text-center font-sarabun text-black">
          <div className="flex flex-col items-center justify-center mb-4">
              <img 
                  src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Garuda_Emblem_of_Thailand.svg/200px-Garuda_Emblem_of_Thailand.svg.png" 
                  alt="Garuda" 
                  className="w-[2.5cm] h-auto mb-2"
              />
              <h1 className="text-xl font-bold tracking-wide mt-2">บันทึกข้อความ (Memorandum)</h1>
              <h2 className="text-lg font-bold">กลุ่มสัจจะออมทรัพย์บ้านไสใหญ่</h2>
          </div>
          <div className="flex justify-between items-start text-sm border-b-2 border-black pb-2 mb-4">
              <div className="text-left">
                  <p><b>ส่วนราชการ:</b> ฝ่ายบัญชีและการเงิน</p>
                  <p><b>ที่:</b> สอ.บสญ/รายงาน/{new Date().getFullYear()+543}</p>
              </div>
              <div className="text-right">
                  <p><b>วันที่:</b> {new Date().toLocaleDateString('th-TH', {year:'numeric', month:'long', day:'numeric'})}</p>
              </div>
          </div>
          <div className="text-left mb-6">
              <p className="text-lg font-bold">เรื่อง: <span className="font-normal">
              {activeTab === 'STATEMENTS' ? 'รายงานสถานะทางการเงิน (Financial Statements)' : 
               activeTab === 'JOURNAL' ? 'รายงานรับ-จ่าย (Income-Expense Report)' :
               activeTab === 'DIVIDENDS' ? 'รายงานการปันผล (Dividend Distribution)' : 'รายงานระบบ'}
              </span></p>
          </div>
      </div>

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-2 print:hidden">
        <div>
            <h2 className="text-3xl font-bold text-gray-800 tracking-tight">Accounting & Reports</h2>
            <p className="text-sm text-gray-500 mt-1 font-medium">P&L, Dividends & General Ledger</p>
        </div>
        {canExport && (
            <div className="flex gap-2 w-full sm:w-auto mt-2 sm:mt-0">
                {activeTab !== 'SETTINGS' && (
                    <button 
                        onClick={handleExcelExport}
                        className="flex-1 sm:flex-none flex items-center justify-center space-x-2 bg-emerald-600 border border-emerald-600 text-white px-5 py-2.5 rounded-xl hover:bg-emerald-700 shadow-sm transition font-bold"
                    >
                        <FileSpreadsheet className="w-4 h-4" />
                        <span>Export Excel</span>
                    </button>
                )}
                <button 
                    onClick={() => window.print()}
                    className="flex-1 sm:flex-none flex items-center justify-center space-x-2 bg-white border border-gray-200 text-gray-700 px-5 py-2.5 rounded-xl hover:bg-gray-50 shadow-sm transition font-bold"
                >
                    <Printer className="w-4 h-4 text-gray-500" />
                    <span>Print PDF</span>
                </button>
            </div>
        )}
      </div>

      {/* Tabs - RESTRICTED FOR OFFICER */}
      <div className="flex border-b-2 border-gray-100 overflow-x-auto no-scrollbar gap-8 print:hidden">
          {userRole !== UserRole.OFFICER && (
              <button onClick={() => setActiveTab('STATEMENTS')} className={`pb-3 px-1 whitespace-nowrap text-sm font-bold border-b-2 transition-all transform translate-y-[2px] ${activeTab === 'STATEMENTS' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-400 hover:text-gray-600'}`}>
                Financial Statements
              </button>
          )}
          <button onClick={() => setActiveTab('JOURNAL')} className={`pb-3 px-1 whitespace-nowrap text-sm font-bold border-b-2 transition-all transform translate-y-[2px] ${activeTab === 'JOURNAL' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-400 hover:text-gray-600'}`}>
            {userRole === UserRole.OFFICER ? 'Daily Transactions (General)' : 'Income-Expense Report'}
          </button>
          {userRole !== UserRole.OFFICER && (
              <>
                <button onClick={() => setActiveTab('DIVIDENDS')} className={`pb-3 px-1 whitespace-nowrap text-sm font-bold border-b-2 transition-all transform translate-y-[2px] ${activeTab === 'DIVIDENDS' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-400 hover:text-gray-600'}`}>
                    Dividend Management
                </button>
                <button onClick={() => setActiveTab('SETTINGS')} className={`pb-3 px-1 whitespace-nowrap text-sm font-bold border-b-2 transition-all transform translate-y-[2px] ${activeTab === 'SETTINGS' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-400 hover:text-gray-600'}`}>
                    System Settings
                </button>
              </>
          )}
      </div>

      {activeTab === 'STATEMENTS' && userRole !== UserRole.OFFICER && (
        <div className="space-y-6">
            {/* Report Date Filter */}
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-wrap items-center gap-4 print:hidden">
                <span className="text-sm font-bold text-gray-700 flex items-center gap-2">
                    <Filter className="w-4 h-4" /> Report Period:
                </span>
                <input 
                    type="date" 
                    value={reportStartDate} 
                    onChange={(e) => setReportStartDate(e.target.value)}
                    className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm outline-none focus:ring-2 focus:ring-emerald-500"
                />
                <span className="text-gray-400">-</span>
                <input 
                    type="date" 
                    value={reportEndDate} 
                    onChange={(e) => setReportEndDate(e.target.value)}
                    className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm outline-none focus:ring-2 focus:ring-emerald-500"
                />
                <button className="text-sm text-emerald-600 font-bold hover:underline ml-auto">
                    Apply Filter
                </button>
            </div>

            <div className="print:shadow-none print:border-none">
                <FinancialStatements 
                    assets={assets}
                    liabilities={liabilities}
                    equity={totalEquity}
                    netProfit={netProfit}
                    cashAssets={cashAssets}
                    otherLiabilities={otherLiabilities}
                    otherEquity={otherEquity}
                    revenueAccounts={revenueAccounts}
                    expenseAccounts={expenseAccounts}
                    assetCompositionData={assetCompositionData}
                    performanceData={performanceData}
                />
            </div>
        </div>
      )}

      {activeTab === 'JOURNAL' && (
          <JournalReport 
            transactions={transactions}
            canRecord={canRecordJournal}
            onRecordNew={() => setShowTransactionModal(true)}
            onPrintReceipt={handlePrintReceipt}
          />
      )}

      {activeTab === 'DIVIDENDS' && userRole !== UserRole.OFFICER && (
           <DividendManager 
             members={MOCK_MEMBERS}
             transactions={transactions}
             netProfit={netProfit}
             totalShares={totalShares}
             canExec={canExecDividend}
             canEdit={canEditDividendRate}
           />
      )}

      {activeTab === 'SETTINGS' && userRole !== UserRole.OFFICER && (
          <AccountingSettings 
            accounts={accounts}
            canManage={canManageAccounts}
            onAddAccountClick={() => setShowAddAccountModal(true)}
            onDeleteAccount={handleDeleteAccount}
            onPrintClosingReport={handlePrintClosingReport}
            revenue={revenue}
            expenses={expenses}
            netProfit={netProfit}
          />
      )}

      {/* Modals */}
      <TransactionModal 
        isOpen={showTransactionModal} 
        onClose={() => setShowTransactionModal(false)}
        accounts={accounts}
        onSave={handleSaveTransaction}
      />

      <AccountModal 
        isOpen={showAddAccountModal} 
        onClose={() => setShowAddAccountModal(false)} 
        onSave={handleAddAccount}
      />

      <ReceiptModal 
        isOpen={showReceipt} 
        onClose={() => setShowReceipt(false)} 
        data={receiptData}
        type={receiptType}
      />
    </div>
  );
};

export default AccountingReports;
