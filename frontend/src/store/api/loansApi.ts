import { apiSlice } from './apiSlice';
import { 
  Loan, 
  LoanFormData, 
  LoanStatus, 
  LoanType,
  LoanBalance,
  Collateral,
  Guarantor,
  PaginatedResponse, 
  FilterParams,
  ApiResponse 
} from '@/types';

export const loansApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Get all loans with pagination and filtering
    getLoans: builder.query<PaginatedResponse<Loan>, FilterParams>({
      query: (params) => ({
        url: '/loans',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'applicationDate',
          order: params.order || 'desc',
          search: params.search,
          status: params.status,
          type: params.type,
          startDate: params.startDate,
          endDate: params.endDate,
          memberId: params.memberId,
        },
      }),
      providesTags: ['Loan'],
    }),

    // Get loan by ID
    getLoanById: builder.query<Loan, string>({
      query: (id) => `/loans/${id}`,
      providesTags: (result, error, id) => [{ type: 'Loan', id }],
    }),

    // Create new loan application
    createLoan: builder.mutation<Loan, LoanFormData>({
      query: (loanData) => ({
        url: '/loans',
        method: 'POST',
        body: loanData,
      }),
      invalidatesTags: ['Loan', 'Dashboard'],
    }),

    // Update loan
    updateLoan: builder.mutation<Loan, { id: string; data: Partial<LoanFormData> }>({
      query: ({ id, data }) => ({
        url: `/loans/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Loan', id }, 'Loan'],
    }),

    // Delete loan
    deleteLoan: builder.mutation<void, string>({
      query: (id) => ({
        url: `/loans/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Loan'],
    }),

    // Approve loan
    approveLoan: builder.mutation<Loan, { id: string; approvalData: { approvedBy: string; notes?: string } }>({
      query: ({ id, approvalData }) => ({
        url: `/loans/${id}/approve`,
        method: 'PATCH',
        body: approvalData,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Loan', id }, 'Loan', 'Dashboard'],
    }),

    // Reject loan
    rejectLoan: builder.mutation<Loan, { id: string; rejectionData: { rejectedBy: string; reason: string } }>({
      query: ({ id, rejectionData }) => ({
        url: `/loans/${id}/reject`,
        method: 'PATCH',
        body: rejectionData,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Loan', id }, 'Loan'],
    }),

    // Disburse loan
    disburseLoan: builder.mutation<Loan, { id: string; disbursementData: { disbursedBy: string; disbursementDate: string; notes?: string } }>({
      query: ({ id, disbursementData }) => ({
        url: `/loans/${id}/disburse`,
        method: 'PATCH',
        body: disbursementData,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Loan', id }, 'Loan', 'Dashboard'],
    }),

    // Get loan balance
    getLoanBalance: builder.query<LoanBalance, string>({
      query: (loanId) => `/loans/${loanId}/balance`,
      providesTags: (result, error, loanId) => [{ type: 'LoanBalance', id: loanId }],
    }),

    // Get loan collateral
    getLoanCollateral: builder.query<Collateral[], string>({
      query: (loanId) => `/loans/${loanId}/collateral`,
      providesTags: (result, error, loanId) => [{ type: 'Collateral', id: loanId }],
    }),

    // Add collateral to loan
    addCollateral: builder.mutation<Collateral, { loanId: string; collateralData: Partial<Collateral> }>({
      query: ({ loanId, collateralData }) => ({
        url: `/loans/${loanId}/collateral`,
        method: 'POST',
        body: collateralData,
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Collateral', id: loanId }, 'Loan'],
    }),

    // Update collateral
    updateCollateral: builder.mutation<Collateral, { loanId: string; collateralId: string; data: Partial<Collateral> }>({
      query: ({ loanId, collateralId, data }) => ({
        url: `/loans/${loanId}/collateral/${collateralId}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Collateral', id: loanId }],
    }),

    // Delete collateral
    deleteCollateral: builder.mutation<void, { loanId: string; collateralId: string }>({
      query: ({ loanId, collateralId }) => ({
        url: `/loans/${loanId}/collateral/${collateralId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Collateral', id: loanId }],
    }),

    // Get loan guarantors
    getLoanGuarantors: builder.query<Guarantor[], string>({
      query: (loanId) => `/loans/${loanId}/guarantors`,
      providesTags: (result, error, loanId) => [{ type: 'Guarantor', id: loanId }],
    }),

    // Add guarantor to loan
    addGuarantor: builder.mutation<Guarantor, { loanId: string; guarantorData: Partial<Guarantor> }>({
      query: ({ loanId, guarantorData }) => ({
        url: `/loans/${loanId}/guarantors`,
        method: 'POST',
        body: guarantorData,
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Guarantor', id: loanId }, 'Loan'],
    }),

    // Update guarantor
    updateGuarantor: builder.mutation<Guarantor, { loanId: string; guarantorId: string; data: Partial<Guarantor> }>({
      query: ({ loanId, guarantorId, data }) => ({
        url: `/loans/${loanId}/guarantors/${guarantorId}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Guarantor', id: loanId }],
    }),

    // Delete guarantor
    deleteGuarantor: builder.mutation<void, { loanId: string; guarantorId: string }>({
      query: ({ loanId, guarantorId }) => ({
        url: `/loans/${loanId}/guarantors/${guarantorId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Guarantor', id: loanId }],
    }),

    // Get loan statistics
    getLoanStats: builder.query<{
      totalLoans: number;
      activeLoans: number;
      pendingLoans: number;
      approvedLoans: number;
      rejectedLoans: number;
      disbursedLoans: number;
      completedLoans: number;
      defaultedLoans: number;
      totalLoanPortfolio: number;
      overdueAmount: number;
      loansByStatus: Record<LoanStatus, number>;
      loansByType: Record<LoanType, number>;
      newApplicationsThisMonth: number;
      monthlyDisbursements: number;
    }, void>({
      query: () => '/loans/stats',
      providesTags: ['Dashboard'],
    }),

    // Get pending loans for approval
    getPendingLoans: builder.query<PaginatedResponse<Loan>, Partial<FilterParams>>({
      query: (params) => ({
        url: '/loans/pending',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'applicationDate',
          order: params.order || 'asc',
        },
      }),
      providesTags: ['Loan'],
    }),

    // Get overdue loans
    getOverdueLoans: builder.query<PaginatedResponse<Loan>, Partial<FilterParams>>({
      query: (params) => ({
        url: '/loans/overdue',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'dueDate',
          order: params.order || 'asc',
        },
      }),
      providesTags: ['Loan'],
    }),

    // Calculate loan payment schedule
    calculateLoanSchedule: builder.query<{
      schedule: Array<{
        paymentNumber: number;
        paymentDate: string;
        principalAmount: number;
        interestAmount: number;
        totalAmount: number;
        balanceAfter: number;
      }>;
      totalInterest: number;
      totalPayment: number;
    }, { amount: number; interestRate: number; term: number; startDate: string }>({
      query: (params) => ({
        url: '/loans/calculate-schedule',
        params,
      }),
    }),

    // Upload loan document
    uploadLoanDocument: builder.mutation<{
      id: string;
      filename: string;
      url: string;
    }, { loanId: string; file: File; documentType: string }>({
      query: ({ loanId, file, documentType }) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('documentType', documentType);
        
        return {
          url: `/loans/${loanId}/documents`,
          method: 'POST',
          body: formData,
          formData: true,
        };
      },
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Loan', id: loanId }],
    }),

    // Get loan documents
    getLoanDocuments: builder.query<any[], string>({
      query: (loanId) => `/loans/${loanId}/documents`,
      providesTags: (result, error, loanId) => [{ type: 'Loan', id: loanId }],
    }),

    // Delete loan document
    deleteLoanDocument: builder.mutation<void, { loanId: string; documentId: string }>({
      query: ({ loanId, documentId }) => ({
        url: `/loans/${loanId}/documents/${documentId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, { loanId }) => [{ type: 'Loan', id: loanId }],
    }),
  }),
});

export const {
  useGetLoansQuery,
  useGetLoanByIdQuery,
  useCreateLoanMutation,
  useUpdateLoanMutation,
  useDeleteLoanMutation,
  useApproveLoanMutation,
  useRejectLoanMutation,
  useDisburseLoanMutation,
  useGetLoanBalanceQuery,
  useGetLoanCollateralQuery,
  useAddCollateralMutation,
  useUpdateCollateralMutation,
  useDeleteCollateralMutation,
  useGetLoanGuarantorsQuery,
  useAddGuarantorMutation,
  useUpdateGuarantorMutation,
  useDeleteGuarantorMutation,
  useGetLoanStatsQuery,
  useGetPendingLoansQuery,
  useGetOverdueLoansQuery,
  useCalculateLoanScheduleQuery,
  useUploadLoanDocumentMutation,
  useGetLoanDocumentsQuery,
  useDeleteLoanDocumentMutation,
} = loansApi;
