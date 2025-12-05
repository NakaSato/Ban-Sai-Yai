import React, { useState, useEffect } from 'react';
import { useGetMinimumInterestQuery } from '@/store/api/dashboardApi';

interface LoanPaymentModalProps {
  memberId: number;
  loanPrincipal: number;
  onClose: () => void;
  onSubmit: (loanId: number, principalAmount: number, interestAmount: number, fineAmount: number, notes: string) => void;
}

const LoanPaymentModal: React.FC<LoanPaymentModalProps> = ({ 
  memberId, 
  loanPrincipal, 
  onClose, 
  onSubmit 
}) => {
  const [loanId, setLoanId] = useState<string>('');
  const [principalAmount, setPrincipalAmount] = useState<string>('');
  const [interestAmount, setInterestAmount] = useState<string>('');
  const [fineAmount, setFineAmount] = useState<string>('0');
  const [notes, setNotes] = useState<string>('');

  // Get minimum interest when loan ID is entered
  const { data: minimumInterestData } = useGetMinimumInterestQuery(
    Number(loanId),
    { skip: !loanId || isNaN(Number(loanId)) }
  );

  // Auto-populate interest amount when minimum interest is calculated
  useEffect(() => {
    if (minimumInterestData?.minimumInterest && !interestAmount) {
      setInterestAmount(minimumInterestData.minimumInterest.toFixed(2));
    }
  }, [minimumInterestData, interestAmount]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const loanIdNum = parseInt(loanId);
    const principalNum = parseFloat(principalAmount);
    const interestNum = parseFloat(interestAmount);
    const fineNum = parseFloat(fineAmount);

    if (isNaN(loanIdNum) || loanIdNum <= 0) {
      alert('Please enter a valid loan ID');
      return;
    }

    if (isNaN(principalNum) || principalNum < 0) {
      alert('Please enter a valid principal amount');
      return;
    }

    if (isNaN(interestNum) || interestNum < 0) {
      alert('Please enter a valid interest amount');
      return;
    }

    if (isNaN(fineNum) || fineNum < 0) {
      alert('Please enter a valid fine amount');
      return;
    }

    onSubmit(loanIdNum, principalNum, interestNum, fineNum, notes);
  };

  const totalPayment = 
    (parseFloat(principalAmount) || 0) + 
    (parseFloat(interestAmount) || 0) + 
    (parseFloat(fineAmount) || 0);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold">Process Loan Payment</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl"
          >
            ×
          </button>
        </div>

        <div className="mb-4 p-3 bg-blue-50 rounded-md">
          <p className="text-sm text-gray-700">
            Outstanding Loan Principal: <span className="font-semibold">฿{loanPrincipal.toLocaleString()}</span>
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Loan ID
            </label>
            <input
              type="number"
              value={loanId}
              onChange={(e) => setLoanId(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter loan ID"
              required
              autoFocus
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Principal Amount (฿)
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={principalAmount}
              onChange={(e) => setPrincipalAmount(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter principal amount"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Interest Amount (฿)
              {minimumInterestData && (
                <span className="ml-2 text-xs text-gray-500">
                  (Min: ฿{minimumInterestData.minimumInterest.toFixed(2)})
                </span>
              )}
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={interestAmount}
              onChange={(e) => setInterestAmount(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter interest amount"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Fine Amount (฿)
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={fineAmount}
              onChange={(e) => setFineAmount(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter fine amount"
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Notes (Optional)
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter notes"
              rows={2}
            />
          </div>

          <div className="mb-6 p-3 bg-gray-50 rounded-md">
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium text-gray-700">Total Payment:</span>
              <span className="text-lg font-bold text-blue-600">
                ฿{totalPayment.toFixed(2)}
              </span>
            </div>
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-200 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-300 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
            >
              Submit Payment
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoanPaymentModal;
