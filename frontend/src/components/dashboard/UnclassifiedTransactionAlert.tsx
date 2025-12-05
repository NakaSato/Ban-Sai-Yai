import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useGetUnclassifiedCountQuery } from '../../store/api/dashboardApi';

const UnclassifiedTransactionAlert: React.FC = () => {
  const navigate = useNavigate();
  const { data: unclassifiedData, isLoading, error, refetch } = useGetUnclassifiedCountQuery();

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
          <div className="h-6 bg-gray-200 rounded w-3/4 mb-4"></div>
          <div className="h-4 bg-gray-200 rounded w-1/2"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="text-red-600">
          <p className="font-semibold">Error loading unclassified transactions</p>
          <p className="text-sm">Please try again later</p>
        </div>
      </div>
    );
  }

  const count = unclassifiedData?.count || 0;

  // Navigate to journal entry screen with pre-filter for unclassified transactions
  const handleNavigateToJournalEntry = () => {
    navigate('/journal-entry?filter=unclassified');
  };

  // If no unclassified transactions, show success state
  if (count === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-800">Transaction Classification</h2>
          <button
            onClick={() => refetch()}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors duration-200"
            title="Refresh"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
        </div>

        <div className="flex items-center p-4 bg-green-50 rounded-lg border-2 border-green-200">
          <svg className="w-8 h-8 text-green-600 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <p className="font-semibold text-green-800">All Transactions Classified</p>
            <p className="text-sm text-green-700 mt-1">
              All transactions have been properly mapped to accounting codes.
            </p>
          </div>
        </div>
      </div>
    );
  }

  // Show warning state when there are unclassified transactions
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-bold text-gray-800">Transaction Classification</h2>
        <button
          onClick={() => refetch()}
          className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors duration-200"
          title="Refresh"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </button>
      </div>

      <div 
        onClick={handleNavigateToJournalEntry}
        className="cursor-pointer hover:shadow-lg transition-shadow duration-200"
      >
        <div className="flex items-start p-4 bg-amber-50 rounded-lg border-2 border-amber-300 hover:border-amber-400">
          <svg className="w-8 h-8 text-amber-600 mr-3 flex-shrink-0 mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <div className="flex-1">
            <p className="font-semibold text-amber-900 text-lg">
              You have {count} unclassified transaction{count !== 1 ? 's' : ''}
            </p>
            <p className="text-sm text-amber-800 mt-2">
              These transactions have not been mapped to accounting codes. Click here to review and classify them in the Journal Entry screen.
            </p>
            <div className="mt-3 flex items-center text-amber-700 font-medium">
              <span className="text-sm">Go to Journal Entry</span>
              <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      {/* Additional Information */}
      <div className="mt-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
        <div className="flex items-start">
          <svg className="w-5 h-5 text-blue-600 mr-2 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <p className="text-sm font-semibold text-blue-800">Why classify transactions?</p>
            <p className="text-xs text-blue-700 mt-1">
              Proper classification ensures accurate financial reporting and helps maintain the integrity of your accounting records. 
              All cash movements must be mapped to appropriate accounting codes for the trial balance to be accurate.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UnclassifiedTransactionAlert;
