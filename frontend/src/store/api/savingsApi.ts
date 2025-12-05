import { apiSlice } from './apiSlice';
import { 
  SavingAccount, 
  SavingTransaction,
  SavingBalance,
  AccountType,
  AccountStatus,
  TransactionType,
  PaginatedResponse, 
  FilterParams,
  ApiResponse 
} from '@/types';

export const savingsApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Get all savings accounts with pagination and filtering
    getSavingsAccounts: builder.query<PaginatedResponse<SavingAccount>, FilterParams>({
      query: (params) => ({
        url: '/savings',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'createdAt',
          order: params.order || 'desc',
          search: params.search,
          status: params.status,
          type: params.type,
          memberId: params.memberId,
          startDate: params.startDate,
          endDate: params.endDate,
        },
      }),
      providesTags: ['SavingAccount'],
    }),

    // Get savings account by ID
    getSavingsAccountById: builder.query<SavingAccount, string>({
      query: (id) => `/savings/${id}`,
      providesTags: (result, error, id) => [{ type: 'SavingAccount', id }],
    }),

    // Create new savings account
    createSavingsAccount: builder.mutation<SavingAccount, { 
      memberId: string; 
      accountType: AccountType;
      initialDeposit?: number;
    }>({
      query: (accountData) => ({
        url: '/savings',
        method: 'POST',
        body: accountData,
      }),
      invalidatesTags: ['SavingAccount', 'Dashboard'],
    }),

    // Update savings account
    updateSavingsAccount: builder.mutation<SavingAccount, { 
      id: string; 
      data: Partial<SavingAccount> 
    }>({
      query: ({ id, data }) => ({
        url: `/savings/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'SavingAccount', id }, 'SavingAccount'],
    }),

    // Close savings account
    closeSavingsAccount: builder.mutation<SavingAccount, { 
      id: string; 
      reason: string;
    }>({
      query: ({ id, reason }) => ({
        url: `/savings/${id}/close`,
        method: 'PATCH',
        body: { reason },
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'SavingAccount', id }, 'SavingAccount'],
    }),

    // Freeze/unfreeze savings account
    toggleAccountFreeze: builder.mutation<SavingAccount, { 
      id: string; 
      frozen: boolean; 
      reason?: string;
    }>({
      query: ({ id, frozen, reason }) => ({
        url: `/savings/${id}/freeze`,
        method: 'PATCH',
        body: { frozen, reason },
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'SavingAccount', id }, 'SavingAccount'],
    }),

    // Get account balance
    getAccountBalance: builder.query<SavingBalance, string>({
      query: (accountId) => `/savings/${accountId}/balance`,
      providesTags: (result, error, accountId) => [{ type: 'SavingBalance', id: accountId }],
    }),

    // Get account transactions
    getAccountTransactions: builder.query<PaginatedResponse<SavingTransaction>, { 
      accountId: string;
      params?: Partial<FilterParams>;
    }>({
      query: ({ accountId, params }) => ({
        url: `/savings/${accountId}/transactions`,
        params: {
          page: params?.page || 0,
          size: params?.size || 10,
          sort: params?.sort || 'transactionDate',
          order: params?.order || 'desc',
          search: params?.search,
          type: params?.type,
          startDate: params?.startDate,
          endDate: params?.endDate,
        },
      }),
      providesTags: (result, error, { accountId }) => [{ type: 'SavingTransaction', id: accountId }],
    }),

    // Deposit to savings account
    depositToAccount: builder.mutation<SavingTransaction, {
      accountId: string;
      amount: number;
      description?: string;
      referenceNumber?: string;
    }>({
      query: ({ accountId, ...depositData }) => ({
        url: `/savings/${accountId}/deposit`,
        method: 'POST',
        body: depositData,
      }),
      invalidatesTags: (result, error, { accountId }) => [
        { type: 'SavingAccount', id: accountId },
        { type: 'SavingBalance', id: accountId },
        { type: 'SavingTransaction', id: accountId },
        'Dashboard'
      ],
    }),

    // Withdraw from savings account
    withdrawFromAccount: builder.mutation<SavingTransaction, {
      accountId: string;
      amount: number;
      description?: string;
      referenceNumber?: string;
    }>({
      query: ({ accountId, ...withdrawData }) => ({
        url: `/savings/${accountId}/withdraw`,
        method: 'POST',
        body: withdrawData,
      }),
      invalidatesTags: (result, error, { accountId }) => [
        { type: 'SavingAccount', id: accountId },
        { type: 'SavingBalance', id: accountId },
        { type: 'SavingTransaction', id: accountId },
        'Dashboard'
      ],
    }),

    // Transfer between accounts
    transferBetweenAccounts: builder.mutation<{
      fromTransaction: SavingTransaction;
      toTransaction: SavingTransaction;
    }, {
      fromAccountId: string;
      toAccountId: string;
      amount: number;
      description?: string;
    }>({
      query: (transferData) => ({
        url: '/savings/transfer',
        method: 'POST',
        body: transferData,
      }),
      invalidatesTags: (result, error, { fromAccountId, toAccountId }) => [
        { type: 'SavingAccount', id: fromAccountId },
        { type: 'SavingAccount', id: toAccountId },
        { type: 'SavingBalance', id: fromAccountId },
        { type: 'SavingBalance', id: toAccountId },
        { type: 'SavingTransaction', id: fromAccountId },
        { type: 'SavingTransaction', id: toAccountId },
      ],
    }),

    // Calculate interest
    calculateInterest: builder.mutation<SavingTransaction, {
      accountId: string;
      interestRate?: number;
    }>({
      query: ({ accountId, interestRate }) => ({
        url: `/savings/${accountId}/calculate-interest`,
        method: 'POST',
        body: { interestRate },
      }),
      invalidatesTags: (result, error, { accountId }) => [
        { type: 'SavingAccount', id: accountId },
        { type: 'SavingBalance', id: accountId },
        { type: 'SavingTransaction', id: accountId },
      ],
    }),

    // Get savings statistics
    getSavingsStats: builder.query<{
      totalSavings: number;
      activeAccounts: number;
      inactiveAccounts: number;
      frozenAccounts: number;
      totalDepositsThisMonth: number;
      totalWithdrawalsThisMonth: number;
      interestAccruedThisMonth: number;
      accountsByType: Record<AccountType, number>;
      accountsByStatus: Record<AccountStatus, number>;
      averageBalance: number;
    }, void>({
      query: () => '/savings/statistics',
      providesTags: ['Dashboard'],
    }),

    // Get member's savings accounts
    getMemberSavingsAccounts: builder.query<SavingAccount[], string>({
      query: (memberId) => `/members/${memberId}/savings`,
      providesTags: ['SavingAccount'],
    }),

    // Get transaction summary
    getTransactionSummary: builder.query<{
      totalDeposits: number;
      totalWithdrawals: number;
      totalInterest: number;
      netChange: number;
      transactionCount: number;
      averageTransaction: number;
    }, {
      accountId: string;
      startDate?: string;
      endDate?: string;
    }>({
      query: ({ accountId, startDate, endDate }) => ({
        url: `/savings/${accountId}/transaction-summary`,
        params: { startDate, endDate },
      }),
      providesTags: (result, error, { accountId }) => [{ type: 'SavingTransaction', id: accountId }],
    }),

    // Get account statement
    getAccountStatement: builder.query<{
      account: SavingAccount;
      transactions: SavingTransaction[];
      openingBalance: number;
      closingBalance: number;
      period: {
        startDate: string;
        endDate: string;
      };
    }, {
      accountId: string;
      startDate: string;
      endDate: string;
    }>({
      query: ({ accountId, startDate, endDate }) => ({
        url: `/savings/${accountId}/statement`,
        params: { startDate, endDate },
      }),
      providesTags: (result, error, { accountId }) => [{ type: 'SavingTransaction', id: accountId }],
    }),

    // Export transactions
    exportTransactions: builder.mutation<Blob, {
      accountId: string;
      format: 'pdf' | 'excel' | 'csv';
      startDate?: string;
      endDate?: string;
    }>({
      query: ({ accountId, format, startDate, endDate }) => ({
        url: `/savings/${accountId}/export`,
        method: 'POST',
        body: { format, startDate, endDate },
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Reverse transaction
    reverseTransaction: builder.mutation<SavingTransaction, {
      accountId: string;
      transactionId: string;
      reason: string;
    }>({
      query: ({ accountId, transactionId, reason }) => ({
        url: `/savings/${accountId}/transactions/${transactionId}/reverse`,
        method: 'POST',
        body: { reason },
      }),
      invalidatesTags: (result, error, { accountId }) => [
        { type: 'SavingAccount', id: accountId },
        { type: 'SavingBalance', id: accountId },
        { type: 'SavingTransaction', id: accountId },
      ],
    }),

    // Upload account document
    uploadAccountDocument: builder.mutation<{
      id: string;
      filename: string;
      url: string;
    }, { accountId: string; file: File; documentType: string }>({
      query: ({ accountId, file, documentType }) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('documentType', documentType);
        
        return {
          url: `/savings/${accountId}/documents`,
          method: 'POST',
          body: formData,
          formData: true,
        };
      },
      invalidatesTags: (result, error, { accountId }) => [{ type: 'SavingAccount', id: accountId }],
    }),

    // Get account documents
    getAccountDocuments: builder.query<any[], string>({
      query: (accountId) => `/savings/${accountId}/documents`,
      providesTags: (result, error, accountId) => [{ type: 'SavingAccount', id: accountId }],
    }),

    // Delete account document
    deleteAccountDocument: builder.mutation<void, { accountId: string; documentId: string }>({
      query: ({ accountId, documentId }) => ({
        url: `/savings/${accountId}/documents/${documentId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, { accountId }) => [{ type: 'SavingAccount', id: accountId }],
    }),
  }),
});

export const {
  useGetSavingsAccountsQuery,
  useGetSavingsAccountByIdQuery,
  useCreateSavingsAccountMutation,
  useUpdateSavingsAccountMutation,
  useCloseSavingsAccountMutation,
  useToggleAccountFreezeMutation,
  useGetAccountBalanceQuery,
  useGetAccountTransactionsQuery,
  useDepositToAccountMutation,
  useWithdrawFromAccountMutation,
  useTransferBetweenAccountsMutation,
  useCalculateInterestMutation,
  useGetSavingsStatsQuery,
  useGetMemberSavingsAccountsQuery,
  useGetTransactionSummaryQuery,
  useGetAccountStatementQuery,
  useExportTransactionsMutation,
  useReverseTransactionMutation,
  useUploadAccountDocumentMutation,
  useGetAccountDocumentsQuery,
  useDeleteAccountDocumentMutation,
} = savingsApi;
