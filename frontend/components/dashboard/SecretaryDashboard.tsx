
import React, { useEffect, useState } from 'react';
import { TrendingUp, Landmark, Scale, CalendarCheck, Loader2 } from 'lucide-react';
import StatCard from './StatCard';
import { api } from '../../services/api';
import { CHART_DATA } from '../../constants';
import { ViewState } from '../../types';
import { FinancialSummaryChart, SecretaryControls, ReportHistoryTable } from './SecretaryWidgets';

interface SecretaryDashboardProps {
    onNavigate: (view: ViewState) => void;
}

const SecretaryDashboard: React.FC<SecretaryDashboardProps> = ({ onNavigate }) => {
  const [loading, setLoading] = useState(true);
  const [financials, setFinancials] = useState({
      totalAssets: 0,
      totalLiabilities: 0,
      netIncome: 12450, 
      lastClosingDate: '30 Sep'
  });

  useEffect(() => {
    const fetchFinancials = async () => {
        try {
            setLoading(true);
            const [members, loans] = await Promise.all([
                api.members.getAll(),
                api.loans.getAll()
            ]);

            const totalSavings = members.reduce((acc, m) => acc + m.savingsBalance, 0);
            const totalShares = members.reduce((acc, m) => acc + m.shareBalance, 0);
            const totalLoans = loans.reduce((acc, l) => acc + l.remainingBalance, 0);
            
            const cashBalance = totalSavings + totalShares - totalLoans + 5000; // + Reserves

            setFinancials({
                totalAssets: cashBalance + totalLoans,
                totalLiabilities: totalSavings + totalShares + 4500,
                netIncome: 12450,
                lastClosingDate: '30 Sep'
            });
        } catch(e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };
    fetchFinancials();
  }, []);

  const recentReports = [
      { id: 1, name: 'Balance Sheet - Oct 2023', date: '2023-10-31', type: 'PDF' },
      { id: 2, name: 'P&L Statement - Q3 2023', date: '2023-09-30', type: 'XLSX' },
      { id: 3, name: 'Member Dividend Report 2022', date: '2022-12-31', type: 'PDF' },
  ];

  if (loading) {
      return <div className="flex h-96 items-center justify-center"><Loader2 className="w-10 h-10 text-emerald-600 animate-spin" /></div>;
  }

  return (
      <div className="space-y-6 animate-fade-in pb-8">
          <div className="flex flex-col md:flex-row justify-between md:items-center gap-2">
              <div>
                  <h2 className="text-2xl font-bold text-gray-800">Secretary Dashboard</h2>
                  <p className="text-gray-500">Financial Control & Reporting</p>
              </div>
              <span className="text-sm bg-gray-100 px-3 py-1 rounded-full text-gray-500 w-fit">Last updated: {new Date().toLocaleDateString()}</span>
          </div>

          {/* KPIs */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6">
              <StatCard 
                  title="Net Income" 
                  value={`฿${financials.netIncome.toLocaleString()}`} 
                  icon={TrendingUp} 
                  colorClass="bg-gradient-to-br from-emerald-500 to-teal-600"
                  gradient={true}
                  subtext="vs last month +2.5%"
              />
              <StatCard 
                  title="Total Assets" 
                  value={`฿${financials.totalAssets.toLocaleString()}`} 
                  icon={Landmark} 
                  colorClass="bg-blue-50 text-blue-600"
                  subtext="Book Value"
              />
              <StatCard 
                  title="Total Liabilities" 
                  value={`฿${financials.totalLiabilities.toLocaleString()}`} 
                  icon={Scale} 
                  colorClass="bg-red-50 text-red-600"
                  subtext="Savings + Shares"
              />
              <StatCard 
                  title="Last Closing" 
                  value={financials.lastClosingDate} 
                  icon={CalendarCheck} 
                  colorClass="bg-purple-50 text-purple-600"
                  subtext="Status: Completed"
              />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Financial Chart Widget */}
              <div className="lg:col-span-2">
                  <FinancialSummaryChart data={CHART_DATA} />
              </div>

              {/* Controls Widget */}
              <div>
                  <SecretaryControls onNavigate={onNavigate} />
              </div>
          </div>

          {/* Report History Widget */}
          <ReportHistoryTable reports={recentReports} />
      </div>
  );
};

export default SecretaryDashboard;
