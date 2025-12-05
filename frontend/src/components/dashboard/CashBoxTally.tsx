import React, { useState, useEffect } from 'react';
import { useGetCashBoxQuery } from '../../store/api/dashboardApi';

interface DenominationCount {
  denomination: number;
  count: number;
}

const CashBoxTally: React.FC = () => {
  const { data: cashBoxData, isLoading, error, refetch } = useGetCashBoxQuery();
  const [showReconciliation, setShowReconciliation] = useState(false);
  const [denominations, setDenominations] = useState<DenominationCount[]>([
    { denomination: 1000, count: 0 },
    { denomination: 500, count: 0 },
    { denomination: 100, count: 0 },
    { denomination: 50, count: 0 },
    { denomination: 20, count: 0 },
    { denomination: 10, count: 0 },
    { denomination: 5, count: 0 },
    { denomination: 1, count: 0 },
  ]);

  // Auto-refresh every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      refetch();
    }, 30000);

    return () => clearInterval(interval);
  }, [refetch]);

  const handleDenominationChange = (denomination: number, count: string) => {
    const parsedCount = parseInt(count) || 0;
    setDenominations(prev =>
      prev.map(d =>
        d.denomination === denomination ? { ...d, count: parsedCount } : d
      )
    );
  };

  const calculatePhysicalCash = (): number => {
    return denominations.reduce(
      (total, d) => total + d.denomination * d.count,
      0
    );
  };

  const calculateVariance = (): number => {
    const physicalCash = calculatePhysicalCash();
    const netCash = cashBoxData?.netCash || 0;
    return physicalCash - netCash;
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div className="space-y-3">
            <div className="h-4 bg-gray-200 rounded"></div>
            <div className="h-4 bg-gray-200 rounded"></div>
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
          <p className="font-semibold">Error loading cash box data</p>
          <p className="text-sm">Please try again later</p>
        </div>
      </div>
    );
  }

  const totalIn = cashBoxData?.totalIn || 0;
  const totalOut = cashBoxData?.totalOut || 0;
  const netCash = cashBoxData?.netCash || 0;

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold text-gray-800">Cash Box Tally</h2>
        <span className="text-sm text-gray-500">
          {cashBoxData?.date || new Date().toLocaleDateString()}
        </span>
      </div>

      <div className="space-y-4">
        {/* Total Inflows */}
        <div className="flex justify-between items-center p-4 bg-green-50 rounded-lg">
          <div>
            <p className="text-sm text-gray-600">Total Inflows</p>
            <p className="text-2xl font-bold text-green-600">
              ฿{totalIn.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          </div>
          <div className="text-green-600">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
          </div>
        </div>

        {/* Total Outflows */}
        <div className="flex justify-between items-center p-4 bg-red-50 rounded-lg">
          <div>
            <p className="text-sm text-gray-600">Total Outflows</p>
            <p className="text-2xl font-bold text-red-600">
              ฿{totalOut.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          </div>
          <div className="text-red-600">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
            </svg>
          </div>
        </div>

        {/* Net Cash */}
        <div className="flex justify-between items-center p-4 bg-blue-50 rounded-lg border-2 border-blue-200">
          <div>
            <p className="text-sm text-gray-600">Net Cash</p>
            <p className="text-3xl font-bold text-blue-600">
              ฿{netCash.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          </div>
          <div className="text-blue-600">
            <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>

        {/* Count Cash Button */}
        <button
          onClick={() => setShowReconciliation(!showReconciliation)}
          className="w-full py-3 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg transition-colors duration-200 flex items-center justify-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
          </svg>
          {showReconciliation ? 'Hide' : 'Count Cash'}
        </button>

        {/* Denomination Reconciliation Form */}
        {showReconciliation && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">Physical Cash Count</h3>
            
            <div className="space-y-3">
              {denominations.map(d => (
                <div key={d.denomination} className="flex items-center justify-between">
                  <label className="text-sm font-medium text-gray-700 w-24">
                    ฿{d.denomination}
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={d.count}
                    onChange={(e) => handleDenominationChange(d.denomination, e.target.value)}
                    className="w-20 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                  <span className="text-sm text-gray-600 w-32 text-right">
                    = ฿{(d.denomination * d.count).toLocaleString()}
                  </span>
                </div>
              ))}
            </div>

            <div className="mt-6 pt-4 border-t border-gray-300">
              <div className="flex justify-between items-center mb-2">
                <span className="font-semibold text-gray-700">Physical Total:</span>
                <span className="text-xl font-bold text-gray-900">
                  ฿{calculatePhysicalCash().toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </div>
              <div className="flex justify-between items-center mb-2">
                <span className="font-semibold text-gray-700">System Total:</span>
                <span className="text-xl font-bold text-gray-900">
                  ฿{netCash.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </div>
              <div className={`flex justify-between items-center p-3 rounded-lg ${
                calculateVariance() === 0 
                  ? 'bg-green-100' 
                  : 'bg-yellow-100'
              }`}>
                <span className="font-bold text-gray-800">Variance:</span>
                <span className={`text-xl font-bold ${
                  calculateVariance() === 0 
                    ? 'text-green-700' 
                    : calculateVariance() > 0 
                      ? 'text-yellow-700' 
                      : 'text-red-700'
                }`}>
                  {calculateVariance() > 0 ? '+' : ''}
                  ฿{calculateVariance().toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CashBoxTally;
