
import React, { useState, useEffect } from 'react';
import { 
  Receipt, ClipboardList, Landmark, TrendingUp, Search, ArrowRight, Loader2
} from 'lucide-react';
import StatCard from './StatCard';
import { api } from '../../services/api';
import { LoanStatus, Member, Loan, ViewState, Transaction, CollateralDocument, LiquidityDTO, PARAnalysisDTO } from '../../types';
import DocumentViewer from '../DocumentViewer';
import { ApprovalQueueTable, ExecutiveSummary, RiskProfileModal, LoanReviewModal, FinancialSignOffModal } from './PresidentWidgets';

interface PresidentDashboardProps {
    onNavigate: (view: ViewState) => void;
}

const PresidentDashboard: React.FC<PresidentDashboardProps> = ({ onNavigate }) => {
  const [loading, setLoading] = useState(true);
  const [members, setMembers] = useState<Member[]>([]);
  const [loans, setLoans] = useState<Loan[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  
  // New Dashboard States
  const [liquidity, setLiquidity] = useState<LiquidityDTO | null>(null);
  const [parAnalysis, setParAnalysis] = useState<PARAnalysisDTO | null>(null);

  useEffect(() => {
    const fetchData = async () => {
        try {
            setLoading(true);
            const [m, l, t, liq, par] = await Promise.all([
                api.members.getAll(),
                api.loans.getAll(),
                api.accounting.getTransactions(),
                api.dashboard.getLiquidityRatio(),
                api.dashboard.getPARAnalysis()
            ]);
            setMembers(m);
            setLoans(l);
            setTransactions(t);
            setLiquidity(liq);
            setParAnalysis(par);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };
    fetchData();
  }, []);

  // KPIs
  const totalLoans = parAnalysis ? parAnalysis.totalPortfolio : 0;
  const cashOnHand = liquidity ? liquidity.cashAndBank : 0;
  
  const pendingLoans = loans.filter(l => l.status === LoanStatus.PENDING);
  
  const [presidentSearch, setPresidentSearch] = useState('');
  const [showRiskModal, setShowRiskModal] = useState(false);
  const [selectedRiskMember, setSelectedRiskMember] = useState<Member | null>(null);
  
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedReviewLoan, setSelectedReviewLoan] = useState<Loan | null>(null);
  
  const [viewerDoc, setViewerDoc] = useState<CollateralDocument | null>(null);

  // Financial Sign-off States
  const [closingConfirmed, setClosingConfirmed] = useState(false);
  const [dividendApproved, setDividendApproved] = useState(false);
  const [showSignOffModal, setShowSignOffModal] = useState(false);
  const [signOffType, setSignOffType] = useState<'CLOSING' | 'DIVIDEND'>('CLOSING');

  const handleMemberSearch = () => {
      const found = members.find(m => 
          m.fullName.toLowerCase().includes(presidentSearch.toLowerCase()) || 
          m.id.toLowerCase().includes(presidentSearch.toLowerCase())
      );
      if (found) {
          setSelectedRiskMember(found);
          setShowRiskModal(true);
          setPresidentSearch('');
      } else {
          alert('Member not found. Please try ID or Name.');
      }
  };

  const processLoanDecision = (approved: boolean) => {
      if (selectedReviewLoan) {
          setLoans(prev => prev.map(l => 
              l.id === selectedReviewLoan.id 
              ? { ...l, status: approved ? LoanStatus.APPROVED : LoanStatus.REJECTED }
              : l
          ));
          setShowReviewModal(false);
          setSelectedReviewLoan(null);
      }
  };

  const handleOpenSignOff = (type: 'CLOSING' | 'DIVIDEND') => {
      setSignOffType(type);
      setShowSignOffModal(true);
  };

  const handleConfirmSignOff = () => {
      if (signOffType === 'CLOSING') setClosingConfirmed(true);
      if (signOffType === 'DIVIDEND') setDividendApproved(true);
  };

  if (loading) {
      return <div className="flex h-96 items-center justify-center"><Loader2 className="w-10 h-10 text-emerald-600 animate-spin" /></div>;
  }

  return (
    <div className="space-y-6 animate-fade-in pb-8">
        <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
            <div>
                <h2 className="text-2xl font-bold text-gray-800">President Dashboard</h2>
                <p className="text-gray-500">Oversight, Approvals & Inspection</p>
            </div>
            
            {/* Risk Search Widget */}
            <div className="w-full md:w-auto">
                <div className="bg-white p-2 rounded-2xl border border-gray-100 shadow-sm flex items-center focus-within:ring-2 focus-within:ring-emerald-500 transition-all group">
                    <Search className="w-5 h-5 text-gray-400 ml-2 shrink-0 group-focus-within:text-emerald-500 transition-colors" />
                    <input
                        type="text"
                        placeholder="Risk Inspection (ID/Name)..."
                        className="flex-1 outline-none px-3 text-sm text-gray-700 bg-transparent min-w-[250px]"
                        value={presidentSearch}
                        onChange={(e) => setPresidentSearch(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleMemberSearch()}
                    />
                    <button onClick={handleMemberSearch} className="bg-emerald-600 text-white p-2 rounded-xl hover:bg-emerald-700 shadow-sm group/btn">
                        <ArrowRight className="w-4 h-4 transition-transform" />
                    </button>
                </div>
            </div>
        </div>

        {/* KPIs - Adjusted for President's Overview */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6">
            <StatCard 
                title="Group Liquidity" 
                value={`฿${cashOnHand.toLocaleString()}`} 
                icon={Landmark} 
                colorClass="bg-gradient-to-br from-emerald-500 to-teal-600"
                gradient={true}
                subtext="Cash Available"
            />
            <StatCard 
                title="Total Debt Risk" 
                value={`฿${totalLoans.toLocaleString()}`} 
                icon={Receipt} 
                colorClass="bg-red-50 text-red-600"
                subtext="Outstanding Principal"
            />
            <StatCard 
                title="Pending Apps" 
                value={pendingLoans.length} 
                icon={ClipboardList} 
                colorClass="bg-yellow-50 text-yellow-600"
                subtext="Requires Approval"
            />
             <StatCard 
                title="Dividend Rate" 
                value="4.5%" 
                icon={TrendingUp} 
                colorClass="bg-blue-50 text-blue-600"
                subtext="Proposed for Year End"
            />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
                {/* Approval Queue Widget */}
                <ApprovalQueueTable 
                    pendingLoans={pendingLoans} 
                    onNavigate={onNavigate} 
                    onReview={(loan) => { setSelectedReviewLoan(loan); setShowReviewModal(true); }}
                />
            </div>

            {/* Executive Summary Widget (Financial Controls) */}
            <ExecutiveSummary 
                closingConfirmed={closingConfirmed}
                setClosingConfirmed={setClosingConfirmed}
                dividendApproved={dividendApproved}
                setDividendApproved={setDividendApproved}
                onNavigate={onNavigate}
                onOpenSignOff={handleOpenSignOff}
            />
        </div>

        {/* --- MODALS --- */}
        {selectedRiskMember && (
            <RiskProfileModal 
                isOpen={showRiskModal}
                onClose={() => setShowRiskModal(false)}
                member={selectedRiskMember}
                loans={loans}
            />
        )}

        {selectedReviewLoan && (
             <LoanReviewModal 
                isOpen={showReviewModal}
                onClose={() => setShowReviewModal(false)}
                loan={selectedReviewLoan}
                members={members}
                transactions={transactions}
                onDecision={processLoanDecision}
                onViewDocument={setViewerDoc}
             />
        )}

        <FinancialSignOffModal 
            isOpen={showSignOffModal}
            onClose={() => setShowSignOffModal(false)}
            type={signOffType}
            onConfirm={handleConfirmSignOff}
        />
        
        <DocumentViewer 
            isOpen={!!viewerDoc} 
            onClose={() => setViewerDoc(null)} 
            document={viewerDoc} 
        />
    </div>
  );
};

export default PresidentDashboard;
