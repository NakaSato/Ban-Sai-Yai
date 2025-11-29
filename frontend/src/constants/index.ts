// API Constants
export const API_BASE_URL = '/api';
export const API_VERSION = 'v1';

// Authentication Constants
export const AUTH_TOKEN_KEY = 'auth_token';
export const USER_KEY = 'user_data';
export const TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000; // 5 minutes

// Role-based Permissions
export const PERMISSIONS = {
  PRESIDENT: [
    'view_dashboard',
    'manage_members',
    'manage_loans',
    'approve_loans',
    'manage_savings',
    'process_payments',
    'view_reports',
    'manage_system',
    'calculate_dividends',
    'manage_accounting'
  ],
  SECRETARY: [
    'view_dashboard',
    'manage_members',
    'manage_loans',
    'view_savings',
    'process_payments',
    'view_reports',
    'manage_documents'
  ],
  OFFICER: [
    'view_dashboard',
    'view_members',
    'manage_loans',
    'view_savings',
    'process_payments',
    'view_reports'
  ],
  MEMBER: [
    'view_dashboard',
    'view_profile',
    'apply_loans',
    'view_savings',
    'make_payments',
    'view_transactions'
  ]
} as const;

// Application Routes
export const ROUTES = {
  LOGIN: '/login',
  DASHBOARD: '/dashboard',
  MEMBERS: '/members',
  MEMBER_DETAIL: '/members/:id',
  LOANS: '/loans',
  LOAN_DETAIL: '/loans/:id',
  LOAN_APPLICATION: '/loans/apply',
  SAVINGS: '/savings',
  SAVINGS_DETAIL: '/savings/:id',
  PAYMENTS: '/payments',
  PAYMENT_DETAIL: '/payments/:id',
  REPORTS: '/reports',
  ADMIN: '/admin',
  PROFILE: '/profile',
  SETTINGS: '/settings'
} as const;

// Status Options
export const MEMBER_STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'SUSPENDED', label: 'Suspended' }
] as const;

export const LOAN_STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'DISBURSED', label: 'Disbursed' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'DEFAULTED', label: 'Defaulted' }
] as const;

export const LOAN_TYPE_OPTIONS = [
  { value: 'PERSONAL', label: 'Personal Loan' },
  { value: 'BUSINESS', label: 'Business Loan' },
  { value: 'EMERGENCY', label: 'Emergency Loan' },
  { value: 'EDUCATION', label: 'Education Loan' }
] as const;

export const PAYMENT_STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'PAID', label: 'Paid' },
  { value: 'OVERDUE', label: 'Overdue' },
  { value: 'CANCELLED', label: 'Cancelled' }
] as const;

export const ACCOUNT_STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'FROZEN', label: 'Frozen' },
  { value: 'CLOSED', label: 'Closed' }
] as const;

export const ACCOUNT_TYPE_OPTIONS = [
  { value: 'REGULAR', label: 'Regular Savings' },
  { value: 'SPECIAL', label: 'Special Savings' },
  { value: 'FIXED_DEPOSIT', label: 'Fixed Deposit' }
] as const;

export const COLLATERAL_TYPE_OPTIONS = [
  { value: 'REAL_ESTATE', label: 'Real Estate' },
  { value: 'VEHICLE', label: 'Vehicle' },
  { value: 'JEWELRY', label: 'Jewelry' },
  { value: 'EQUIPMENT', label: 'Equipment' },
  { value: 'OTHER', label: 'Other' }
] as const;

export const TRANSACTION_TYPE_OPTIONS = [
  { value: 'DEPOSIT', label: 'Deposit' },
  { value: 'WITHDRAWAL', label: 'Withdrawal' },
  { value: 'INTEREST', label: 'Interest' },
  { value: 'FEE', label: 'Fee' },
  { value: 'TRANSFER_IN', label: 'Transfer In' },
  { value: 'TRANSFER_OUT', label: 'Transfer Out' }
] as const;

// Pagination Constants
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [5, 10, 25, 50, 100];

// Date Formats
export const DATE_FORMATS = {
  DISPLAY: 'MMMM dd, yyyy',
  SHORT: 'MM/dd/yyyy',
  ISO: 'yyyy-MM-dd',
  DATETIME: 'MMMM dd, yyyy hh:mm a',
  TIME: 'hh:mm a'
} as const;

// Currency Constants
export const CURRENCY = {
  CODE: 'PHP',
  SYMBOL: '₱',
  LOCALE: 'en-PH'
} as const;

// Validation Constants
export const VALIDATION = {
  MIN_PASSWORD_LENGTH: 8,
  MAX_FILE_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_FILE_TYPES: ['image/jpeg', 'image/png', 'application/pdf'],
  PHONE_REGEX: /^(\+63|0)[0-9]{10}$/,
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
} as const;

// Loan Constants
export const LOAN_CONSTANTS = {
  MIN_AMOUNT: 1000,
  MAX_AMOUNT: 1000000,
  MIN_TERM: 1,
  MAX_TERM: 60,
  DEFAULT_INTEREST_RATE: 0.10, // 10%
  LATE_PAYMENT_PENALTY_RATE: 0.02 // 2%
} as const;

// Savings Constants
export const SAVINGS_CONSTANTS = {
  MIN_DEPOSIT: 100,
  MIN_BALANCE: 500,
  INTEREST_RATE: 0.03, // 3% annually
  WITHDRAWAL_LIMIT: 50000
} as const;

// UI Constants
export const UI = {
  APP_NAME: 'Bansaiyai Financial System',
  APP_VERSION: '1.0.0',
  SIDEBAR_WIDTH: 240,
  HEADER_HEIGHT: 64,
  CONTAINER_MAX_WIDTH: 1200,
  BREAKPOINTS: {
    XS: 0,
    SM: 600,
    MD: 900,
    LG: 1200,
    XL: 1536
  }
} as const;

// Theme Colors
export const THEME_COLORS = {
  PRIMARY: {
    MAIN: '#1976d2',
    LIGHT: '#42a5f5',
    DARK: '#1565c0'
  },
  SECONDARY: {
    MAIN: '#dc004e',
    LIGHT: '#ff5983',
    DARK: '#9a0036'
  },
  SUCCESS: {
    MAIN: '#2e7d32',
    LIGHT: '#4caf50',
    DARK: '#1b5e20'
  },
  WARNING: {
    MAIN: '#ed6c02',
    LIGHT: '#ff9800',
    DARK: '#e65100'
  },
  ERROR: {
    MAIN: '#d32f2f',
    LIGHT: '#f44336',
    DARK: '#c62828'
  },
  INFO: {
    MAIN: '#0288d1',
    LIGHT: '#03a9f4',
    DARK: '#01579b'
  }
} as const;

// Local Storage Keys
export const STORAGE_KEYS = {
  THEME: 'theme_mode',
  LANGUAGE: 'language',
  SIDEBAR_COLLAPSED: 'sidebar_collapsed',
  RECENT_TRANSACTIONS: 'recent_transactions',
  PREFERENCES: 'user_preferences'
} as const;

// Export All
export const CONSTANTS = {
  API: { API_BASE_URL, API_VERSION },
  AUTH: { AUTH_TOKEN_KEY, USER_KEY, TOKEN_REFRESH_THRESHOLD },
  PERMISSIONS,
  ROUTES,
  STATUS: {
    MEMBER: MEMBER_STATUS_OPTIONS,
    LOAN: LOAN_STATUS_OPTIONS,
    PAYMENT: PAYMENT_STATUS_OPTIONS,
    ACCOUNT: ACCOUNT_STATUS_OPTIONS
  },
  TYPES: {
    LOAN: LOAN_TYPE_OPTIONS,
    ACCOUNT: ACCOUNT_TYPE_OPTIONS,
    COLLATERAL: COLLATERAL_TYPE_OPTIONS,
    TRANSACTION: TRANSACTION_TYPE_OPTIONS
  },
  PAGINATION: { DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS },
  DATE: DATE_FORMATS,
  CURRENCY,
  VALIDATION,
  LOAN: LOAN_CONSTANTS,
  SAVINGS: SAVINGS_CONSTANTS,
  UI,
  THEME: THEME_COLORS,
  STORAGE: STORAGE_KEYS
} as const;
