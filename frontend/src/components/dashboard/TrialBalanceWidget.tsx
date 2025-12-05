import React from 'react';
import { useGetTrialBalanceQuery } from '../../store/api/dashboardApi';

const TrialBalanceWidget: React.FC = () => {
  const { data: trialBalance, isLoading, error, refetch } = useGetTrialBalanceQuery();

  // Auto-refresh every 60 seconds
  React.useEffect(() => {
    const interval = setInterval(() => {
      refetch();
    }, 60000);

    return () => clearInterval(interval);
  }, [refetch]);

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-gray-200 rounded w-1/2 mb-4"></div>
          <div className="space-y-3">
            <div className="h-4 bg-gray-200 rounded"></div>
            <div className="h-8 bg-gray-200 rounded"></div>
            <div className="h-4 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="text-red-600">
          <p className="font-semibold">Error loading trial balance</p>
          <p className="text-sm">Please try again later</p>
        </div>
      </div>
    );
  }

  const totalDebits = trialBalance?.totalDebits || 0;
  const totalCredits = trialBalance?.totalCredits || 0;
  const variance = trialBalance?.variance || 0;
  const isBalanced = trialBalance?.isBalanced ?? true;
  const fiscalPeriod = trialBalance?.fiscalPeriod || 'Unknown';

  // Calculate percentages for progress bar
  const maxValue = Math.max(totalDebits, totalCredits);
  const debitPercentage = maxValue > 0 ? (totalDebits / maxValue) * 100 : 0;
  const creditPercentage = maxValue > 0 ? (totalCredits / maxValue) * 100 : 0;

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold text-gray-800">Trial Balance</h2>
        <span className="text-sm text-gray-500">{fiscalPeriod}</span>
      </div>

      <div className="space-y-6">
        {/* Balanced Status Indicator */}
        {isBalanced ? (
          <div className="flex items-center justify-center p-4 bg-green-50 rounded-lg border-2 border-green-200">
            <svg className="w-6 h-6 text-green-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-lg font-semibold text-green-700">Books are Balanced</span>
          </div>
        ) : (
          <div className="flex items-center justify-center p-4 bg-red-50 rounded-lg border-2 border-red-200">
            <svg className="w-6 h-6 text-red-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-lg font-semibold text-red-700">Books are Unbalanced</span>
          </div>
        )}

        {/* Debits and Credits Display */}
        <div className="space-y-4">
          {/* Total Debits */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-medium text-gray-700">Total Debits</span>
              <span className="text-lg font-bold text-blue-600">
                ฿{totalDebits.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </div>
            {!isBalanced && (
              <div className="w-full bg-gray-200 rounded-full h-3">
                <div
                  className="bg-blue-600 h-3 rounded-full transition-all duration-300"
                  style={{ width: `${debitPercentage}%` }}
                ></div>
              </div>
            )}
          </div>

          {/* Total Credits */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm font-medium text-gray-700">Total Credits</span>
              <span className="text-lg font-bold text-red-600">
                ฿{totalCredits.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </div>
            {!isBalanced && (
              <div className="w-full bg-gray-200 rounded-full h-3">
                <div
                  className="bg-red-600 h-3 rounded-full transition-all duration-300"
                  style={{ width: `${creditPercentage}%` }}
                ></div>
              </div>
            )}
          </div>
        </div>

        {/* Balanced Progress Bar */}
        {isBalanced && (
          <div className="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
            <div className="bg-green-500 h-4 rounded-full w-full flex items-center justify-center">
              <span className="text-xs font-semibold text-white">100% Balanced</span>
            </div>
          </div>
        )}

        {/* Variance Display */}
        {!isBalanced && (
          <div className={`p-4 rounded-lg border-2 ${
            variance === 0 
              ? 'bg-green-50 border-green-200' 
              : 'bg-yellow-50 border-yellow-200'
          }`}>
            <div className="flex justify-between items-center">
              <span className="font-semibold text-gray-700">Variance:</span>
              <span className={`text-xl font-bold ${
                variance === 0 
                  ? 'text-green-700' 
                  : variance > 0 
                    ? 'text-blue-700' 
                    : 'text-red-700'
              }`}>
                {variance > 0 ? '+' : ''}
                ฿{Math.abs(variance).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </div>
            {variance !== 0 && (
              <p className="text-xs text-gray-600 mt-2">
                {variance > 0 ? 'Debits exceed credits' : 'Credits exceed debits'}
              </p>
            )}
          </div>
        )}

        {/* Warning Message for Unbalanced Books */}
        {!isBalanced && (
          <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg">
            <div className="flex items-start">
              <svg className="w-5 h-5 text-amber-600 mr-2 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <div>
                <p className="text-sm font-semibold text-amber-800">Monthly Report Generation Blocked</p>
                <p className="text-xs text-amber-700 mt-1">
                  The trial balance must be balanced before you can generate the monthly financial report. 
                  Please review and correct any accounting entries.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex gap-3">
          <button
            onClick={() => refetch()}
            className="flex-1 py-2 px-4 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium rounded-lg transition-colors duration-200 flex items-center justify-center gap-2"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
          <button
            disabled={!isBalanced}
            className={`flex-1 py-2 px-4 font-medium rounded-lg transition-colors duration-200 flex items-center justify-center gap-2 ${
              isBalanced
                ? 'bg-indigo-600 hover:bg-indigo-700 text-white'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            Generate Report
          </button>
        </div>
      </div>
    </div>
  );
};

export default TrialBalanceWidget;
