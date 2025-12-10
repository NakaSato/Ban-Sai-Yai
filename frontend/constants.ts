import { Member, Loan, LoanStatus, Transaction, Account, Notification } from './types';

export const MOCK_MEMBERS: Member[] = [
  {
    id: 'M001',
    fullName: 'Somsak Jai-dee',
    idCardNumber: '1-1001-00123-45-1',
    birthDate: '1975-05-12',
    address: '123 Ban Sai Yai, Moo 4',
    phoneNumber: '081-234-5678',
    shareBalance: 5000,
    savingsBalance: 12500.50,
    joinedDate: '2015-01-10',
    status: 'ACTIVE',
    isFrozen: false,
    monthlyIncome: 25000,
    occupation: 'Farmer'
  },
  {
    id: 'M002',
    fullName: 'Malee Srikram',
    idCardNumber: '3-2002-00987-65-2',
    birthDate: '1982-11-23',
    address: '45 Ban Sai Yai, Moo 4',
    phoneNumber: '089-987-6543',
    shareBalance: 3000,
    savingsBalance: 800.00,
    joinedDate: '2016-03-15',
    status: 'ACTIVE',
    isFrozen: false,
    monthlyIncome: 18000,
    occupation: 'Merchant'
  },
  {
    id: 'M003',
    fullName: 'Prasert Meekul',
    idCardNumber: '5-3003-11111-22-3',
    birthDate: '1960-02-01',
    address: '88 Ban Sai Yai, Moo 5',
    phoneNumber: '086-555-4444',
    shareBalance: 10000,
    savingsBalance: 45000.00,
    joinedDate: '2010-06-20',
    status: 'ACTIVE',
    isFrozen: false,
    monthlyIncome: 40000,
    occupation: 'Retired Teacher'
  },
  {
    id: 'M004',
    fullName: 'Aree Sampun',
    idCardNumber: '1-4004-55555-66-4',
    birthDate: '1995-08-14',
    address: '12 Ban Sai Yai, Moo 4',
    phoneNumber: '090-111-2222',
    shareBalance: 1500,
    savingsBalance: 250.00,
    joinedDate: '2023-01-05',
    status: 'ACTIVE',
    isFrozen: true,
    monthlyIncome: 15000,
    occupation: 'General Labor'
  }
];

export const MOCK_LOANS: Loan[] = [
  {
    id: 'L001',
    memberId: 'M002',
    memberName: 'Malee Srikram',
    principalAmount: 20000,
    remainingBalance: 15000,
    interestRate: 6.5,
    termMonths: 12,
    startDate: '2023-06-01',
    status: LoanStatus.ACTIVE,
    loanType: 'COMMON',
    contractNo: 'CNT-23-001',
    guarantorIds: ['M001']
  },
  {
    id: 'L002',
    memberId: 'M004',
    memberName: 'Aree Sampun',
    principalAmount: 50000,
    remainingBalance: 48000,
    interestRate: 7.0,
    termMonths: 24,
    startDate: '2023-09-15',
    status: LoanStatus.ACTIVE,
    loanType: 'INVESTMENT',
    contractNo: 'CNT-23-045',
    guarantorIds: ['M003']
  },
  {
    id: 'L003',
    memberId: 'M001',
    memberName: 'Somsak Jai-dee',
    principalAmount: 100000,
    remainingBalance: 100000,
    interestRate: 6.0,
    termMonths: 36,
    startDate: '2023-10-20',
    status: LoanStatus.PENDING,
    loanType: 'COMMON',
    guarantorIds: ['M003', 'M004']
  }
];

export const MOCK_TRANSACTIONS: Transaction[] = [
  { id: 'T001', date: '2023-10-01', type: 'DEPOSIT', amount: 500, memberId: 'M001', description: 'Monthly Savings', receiptId: 'REC-001' },
  { id: 'T002', date: '2023-10-01', type: 'SHARE_PURCHASE', amount: 100, memberId: 'M001', description: 'Share Purchase', receiptId: 'REC-002' },
  { id: 'T003', date: '2023-10-02', type: 'LOAN_REPAYMENT', amount: 2000, memberId: 'M002', description: 'Loan L001 Installment', receiptId: 'REC-003' },
  { id: 'T004', date: '2023-10-05', type: 'WITHDRAWAL', amount: 1000, memberId: 'M003', description: 'Emergency Withdrawal', receiptId: 'REC-004' },
  { id: 'T005', date: '2023-10-06', type: 'SHARE_PURCHASE', amount: 500, memberId: 'M004', description: 'Additional Shares', receiptId: 'REC-005' },
];

export const MOCK_ACCOUNTS: Account[] = [
  { code: '1101', name: 'Cash on Hand', category: 'ASSET', balance: 50000 },
  { code: '1102', name: 'Cash at Bank', category: 'ASSET', balance: 120000 },
  { code: '2101', name: 'Member Savings', category: 'LIABILITY', balance: 85000 },
  { code: '3101', name: 'Share Capital', category: 'EQUITY', balance: 45000 },
  { code: '3201', name: 'Reserve Fund', category: 'EQUITY', balance: 5000 },
  { code: '4101', name: 'Interest Income', category: 'REVENUE', balance: 12500 },
  { code: '4501', name: 'Fee Income', category: 'REVENUE', balance: 500 },
  { code: '5101', name: 'Office Supplies', category: 'EXPENSE', balance: 2500 },
  { code: '5102', name: 'Water & Electric', category: 'EXPENSE', balance: 1200 },
];

export const MOCK_NOTIFICATIONS: Notification[] = [
  { id: 'N1', title: 'Loan Approved', message: 'Your common loan application (L003) has been approved.', date: '2 mins ago', type: 'SUCCESS', read: false },
  { id: 'N2', title: 'Payment Reminder', message: 'Monthly loan repayment for contract CNT-23-001 is due tomorrow.', date: '1 hour ago', type: 'ALERT', read: false },
  { id: 'N3', title: 'Dividend Announcement', message: 'The annual dividend rate has been proposed at 4.5%.', date: '1 day ago', type: 'INFO', read: true },
];

export const CHART_DATA = [
  { name: 'Jan', savings: 4000, loans: 2400 },
  { name: 'Feb', savings: 3000, loans: 1398 },
  { name: 'Mar', savings: 2000, loans: 9800 },
  { name: 'Apr', savings: 2780, loans: 3908 },
  { name: 'May', savings: 1890, loans: 4800 },
  { name: 'Jun', savings: 2390, loans: 3800 },
  { name: 'Jul', savings: 3490, loans: 4300 },
];