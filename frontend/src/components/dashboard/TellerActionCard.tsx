import React, { useState } from 'react';
import { 
  useGetMemberFinancialsQuery,
  useProcessDepositMutation,
  useProcessLoanPaymentMutation,
  useGetMinimumInterestQuery,
  useGetFiscalPeriodQuery
} from '@/store/api/dashboardApi';
import DepositModal from './DepositModal';
import LoanPaymentModal from './LoanPaymentModal';

interface TellerActionCardProps {
  fiscalPeriodOpen?: boolean;
}

const TellerActionCard: React.FC<TellerActionCardProps> = ({ fiscalPeriodOpen = true }) => {
  const [selectedMemberId, setSelectedMemberId] = useState<number | null>(null);
  const [showDepositModal, setShowDepositModal] = useState(false);
  const [showLoanPaymentModal, setShowLoanPaymentModal] = useState(false);

  // Get fiscal period status
  const { data: fiscalPeriod } = useGetFiscalPeriodQuery();
  const isPeriodOpen = fiscalPeriod?.status === 'OPEN';

  // Get member financials when a member is selected
  const { data: memberFinancials, isLoading: isLoadingFinancials } = useGetMemberFinancialsQuery(
    selectedMemberId!,
    { skip: !selectedMemberId }
  );

  const [processDeposit, { isLoading: isProcessingDeposit }] = useProcessDepositMutation();
  const [processLoanPayment, { isLoading: isProcessingPayment }] = useProcessLoanPaymentMutation();

  const handleMemberSelect = (memberId: number) => {
    setSelectedMemberId(memberId);
  };

  const handleDepositClick = () => {
    if (!selectedMemberId) {
      alert('Please select a member first');
      return;
    }
    setShowDepositModal(true);
  };

  const handleLoanPaymentClick = () => {
    if (!selectedMemberId) {
      alert('Please select a member first');
      return;
    }
    if (!memberFinancials?.loanPrincipal || memberFinancials.loanPrincipal === 0) {
      alert('This member has no active loan');
      return;
    }
    setShowLoanPaymentModal(true);
  };

  const handleDepositSubmit = async (amount: number, notes: string) => {
    if (!selectedMemberId) return;

    try {
      const result = await processDeposit({
        memberId: selectedMemberId,
        amount,
        notes
      }).unwrap();

      if (result.status === 'SUCCESS') {
        alert(`Deposit successful! Transaction #${result.transactionNumber}`);
        setShowDepositModal(false);
      } else {
        alert(`Deposit failed: ${result.message}`);
      }
    } catch (error) {
      alert('Error processing deposit');
      console.error('Deposit error:', error);
    }
  };

  const handleLoanPaymentSubmit = async (
    loanId: number,
    principalAmount: number,
    interestAmount: number,
    fineAmount: number,
    notes: string
  ) => {
    if (!selectedMemberId) return;

    try {
      const result = await processLoanPayment({
        memberId: selectedMemberId,
        loanId,
        principalAmount,
        interestAmount,
        fineAmount,
        notes
      }).unwrap();

      if (result.status === 'SUCCESS') {
        alert(`Loan payment successful! Transaction #${result.transactionNumber}`);
        setShowLoanPaymentModal(false);
      } else {
        alert(`Loan payment failed: ${result.message}`);
      }
    } catch (error) {
      alert('Error processing loan payment');
      console.error('Loan payment error:', error);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-xl font-semibold mb-4">Teller Action Card</h2>

      {/* Member Selection */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Select Member
        </label>
        <input
          type="number"
          placeholder="Enter Member ID"
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          onChange={(e) => handleMemberSelect(Number(e.target.value))}
        />
      </div>

      {/* Member Financials Display */}
      {selectedMemberId && (
        <div className="mb-4 p-4 bg-gray-50 rounded-md">
          {isLoadingFinancials ? (
            <p className="text-gray-500">Loading member information...</p>
          ) : memberFinancials ? (
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm font-medium text-gray-700">Savings Balance:</span>
                <span className="text-sm font-semibold text-green-600">
                  ฿{memberFinancials.savingsBalance.toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm font-medium text-gray-700">Loan Principal:</span>
                <span className="text-sm font-semibold text-red-600">
                  ฿{memberFinancials.loanPrincipal.toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm font-medium text-gray-700">Loan Status:</span>
                <span className="text-sm font-semibold text-gray-800">
                  {memberFinancials.loanStatus}
                </span>
              </div>
            </div>
          ) : (
            <p className="text-gray-500">No member data available</p>
          )}
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex gap-3">
        <button
          onClick={handleDepositClick}
          disabled={!isPeriodOpen || !selectedMemberId || isProcessingDeposit}
          className="flex-1 bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
        >
          {isProcessingDeposit ? 'Processing...' : 'Deposit'}
        </button>
        <button
          onClick={handleLoanPaymentClick}
          disabled={!isPeriodOpen || !selectedMemberId || isProcessingPayment}
          className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
        >
          {isProcessingPayment ? 'Processing...' : 'Loan Payment'}
        </button>
      </div>

      {!isPeriodOpen && (
        <p className="mt-3 text-sm text-red-600 text-center">
          Transactions are disabled - Fiscal period is closed
        </p>
      )}

      {/* Modals */}
      {showDepositModal && (
        <DepositModal
          onClose={() => setShowDepositModal(false)}
          onSubmit={handleDepositSubmit}
        />
      )}

      {showLoanPaymentModal && selectedMemberId && memberFinancials && (
        <LoanPaymentModal
          memberId={selectedMemberId}
          loanPrincipal={memberFinancials.loanPrincipal}
          onClose={() => setShowLoanPaymentModal(false)}
          onSubmit={handleLoanPaymentSubmit}
        />
      )}
    </div>
  );
};

export default TellerActionCard;
