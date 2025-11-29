import { apiSlice } from './apiSlice';
import { 
  Payment, 
  PaymentFormData,
  PaymentType,
  PaymentStatus,
  PaginatedResponse, 
  FilterParams,
  ApiResponse 
} from '@/types';

export const paymentsApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Get all payments with pagination and filtering
    getPayments: builder.query<PaginatedResponse<Payment>, FilterParams>({
      query: (params) => ({
        url: '/payments',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'paymentDate',
          order: params.order || 'desc',
          search: params.search,
          status: params.status,
          type: params.type,
          memberId: params.memberId,
          loanId: params.loanId,
          startDate: params.startDate,
          endDate: params.endDate,
        },
      }),
      providesTags: ['Payment'],
    }),

    // Get payment by ID
    getPaymentById: builder.query<Payment, string>({
      query: (id) => `/payments/${id}`,
      providesTags: (result, error, id) => [{ type: 'Payment', id }],
    }),

    // Process payment
    processPayment: builder.mutation<Payment, PaymentFormData>({
      query: (paymentData) => ({
        url: '/payments',
        method: 'POST',
        body: paymentData,
      }),
      invalidatesTags: ['Payment', 'Loan', 'Dashboard'],
    }),

    // Update payment
    updatePayment: builder.mutation<Payment, { 
      id: string; 
      data: Partial<PaymentFormData> 
    }>({
      query: ({ id, data }) => ({
        url: `/payments/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Payment', id }, 'Payment'],
    }),

    // Cancel payment
    cancelPayment: builder.mutation<Payment, { 
      id: string; 
      reason: string;
    }>({
      query: ({ id, reason }) => ({
        url: `/payments/${id}/cancel`,
        method: 'PATCH',
        body: { reason },
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Payment', id }, 'Payment', 'Loan'],
    }),

    // Reverse payment
    reversePayment: builder.mutation<Payment, { 
      id: string; 
      reason: string;
    }>({
      query: ({ id, reason }) => ({
        url: `/payments/${id}/reverse`,
        method: 'POST',
        body: { reason },
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Payment', id }, 'Payment', 'Loan'],
    }),

    // Get payment schedule for a loan
    getPaymentSchedule: builder.query<{
      schedule: Array<{
        paymentNumber: number;
        dueDate: string;
        principalAmount: number;
        interestAmount: number;
        totalAmount: number;
        status: PaymentStatus;
        paidDate?: string;
        paidAmount?: number;
        balanceAfter: number;
      }>;
      summary: {
        totalPayments: number;
        paidPayments: number;
        pendingPayments: number;
        overduePayments: number;
        totalAmount: number;
        paidAmount: number;
        remainingAmount: number;
      };
    }, string>({
      query: (loanId) => `/loans/${loanId}/payment-schedule`,
      providesTags: ['Payment'],
    }),

    // Get overdue payments
    getOverduePayments: builder.query<PaginatedResponse<Payment>, Partial<FilterParams>>({
      query: (params) => ({
        url: '/payments/overdue',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'dueDate',
          order: params.order || 'asc',
        },
      }),
      providesTags: ['Payment'],
    }),

    // Get upcoming payments
    getUpcomingPayments: builder.query<PaginatedResponse<Payment>, {
      days?: number;
      params?: Partial<FilterParams>;
    }>({
      query: ({ days = 30, params }) => ({
        url: '/payments/upcoming',
        params: {
          days,
          page: params?.page || 0,
          size: params?.size || 10,
          sort: params?.sort || 'dueDate',
          order: params?.order || 'asc',
        },
      }),
      providesTags: ['Payment'],
    }),

    // Get payments by loan
    getLoanPayments: builder.query<PaginatedResponse<Payment>, { 
      loanId: string;
      params?: Partial<FilterParams>;
    }>({
      query: ({ loanId, params }) => ({
        url: `/loans/${loanId}/payments`,
        params: {
          page: params?.page || 0,
          size: params?.size || 10,
          sort: params?.sort || 'paymentDate',
          order: params?.order || 'desc',
          status: params?.status,
          type: params?.type,
        },
      }),
      providesTags: (result, error, { loanId }) => [{ type: 'Payment', id: loanId }],
    }),

    // Get member's payments
    getMemberPayments: builder.query<PaginatedResponse<Payment>, { 
      memberId: string;
      params?: Partial<FilterParams>;
    }>({
      query: ({ memberId, params }) => ({
        url: `/members/${memberId}/payments`,
        params: {
          page: params?.page || 0,
          size: params?.size || 10,
          sort: params?.sort || 'paymentDate',
          order: params?.order || 'desc',
          status: params?.status,
          type: params?.type,
          startDate: params?.startDate,
          endDate: params?.endDate,
        },
      }),
      providesTags: ['Payment'],
    }),

    // Get payment statistics
    getPaymentStats: builder.query<{
      totalPayments: number;
      paidPayments: number;
      pendingPayments: number;
      overduePayments: number;
      cancelledPayments: number;
      totalAmount: number;
      paidAmount: number;
      pendingAmount: number;
      overdueAmount: number;
      monthlyCollection: number;
      monthlyTarget: number;
      collectionRate: number;
      paymentsByStatus: Record<PaymentStatus, number>;
      paymentsByType: Record<PaymentType, number>;
    }, { startDate?: string; endDate?: string }>({
      query: ({ startDate, endDate }) => ({
        url: '/payments/stats',
        params: { startDate, endDate },
      }),
      providesTags: ['Dashboard'],
    }),

    // Generate payment receipt
    generateReceipt: builder.mutation<Blob, { paymentId: string; format?: 'pdf' | 'html' }>({
      query: ({ paymentId, format = 'pdf' }) => ({
        url: `/payments/${paymentId}/receipt`,
        method: 'POST',
        body: { format },
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Send payment reminder
    sendPaymentReminder: builder.mutation<void, {
      paymentIds: string[];
      message?: string;
    }>({
      query: ({ paymentIds, message }) => ({
        url: '/payments/send-reminder',
        method: 'POST',
        body: { paymentIds, message },
      }),
    }),

    // Calculate payment amount
    calculatePaymentAmount: builder.query<{
      principalAmount: number;
      interestAmount: number;
      penaltyAmount: number;
      totalAmount: number;
      breakdown: Array<{
        type: string;
        amount: number;
        description: string;
      }>;
    }, {
      loanId: string;
      paymentDate?: string;
      paymentType?: PaymentType;
      amount?: number;
    }>({
      query: ({ loanId, paymentDate, paymentType, amount }) => ({
        url: '/payments/calculate',
        params: { loanId, paymentDate, paymentType, amount },
      }),
    }),

    // Bulk process payments
    bulkProcessPayments: builder.mutation<Payment[], {
      payments: Array<{
        loanId: string;
        amount: number;
        paymentMethod: string;
        notes?: string;
      }>;
    }>({
      query: ({ payments }) => ({
        url: '/payments/bulk',
        method: 'POST',
        body: { payments },
      }),
      invalidatesTags: ['Payment', 'Loan', 'Dashboard'],
    }),

    // Get payment methods
    getPaymentMethods: builder.query<Array<{
      id: string;
      name: string;
      type: string;
      isActive: boolean;
    }>, void>({
      query: () => '/payments/methods',
    }),

    // Export payment history
    exportPaymentHistory: builder.mutation<Blob, {
      format: 'pdf' | 'excel' | 'csv';
      startDate?: string;
      endDate?: string;
      memberId?: string;
      loanId?: string;
      status?: PaymentStatus;
    }>({
      query: (exportData) => ({
        url: '/payments/export',
        method: 'POST',
        body: exportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Verify payment
    verifyPayment: builder.mutation<Payment, {
      paymentId: string;
      verificationCode: string;
    }>({
      query: ({ paymentId, verificationCode }) => ({
        url: `/payments/${paymentId}/verify`,
        method: 'POST',
        body: { verificationCode },
      }),
      invalidatesTags: (result, error, { paymentId }) => [{ type: 'Payment', id: paymentId }],
    }),

    // Get payment audit trail
    getPaymentAuditTrail: builder.query<Array<{
      id: string;
      paymentId: string;
      action: string;
      performedBy: string;
      performedAt: string;
      details: string;
      previousValues?: Record<string, any>;
      newValues?: Record<string, any>;
    }>, string>({
      query: (paymentId) => `/payments/${paymentId}/audit-trail`,
    }),
  }),
});

export const {
  useGetPaymentsQuery,
  useGetPaymentByIdQuery,
  useProcessPaymentMutation,
  useUpdatePaymentMutation,
  useCancelPaymentMutation,
  useReversePaymentMutation,
  useGetPaymentScheduleQuery,
  useGetOverduePaymentsQuery,
  useGetUpcomingPaymentsQuery,
  useGetLoanPaymentsQuery,
  useGetMemberPaymentsQuery,
  useGetPaymentStatsQuery,
  useGenerateReceiptMutation,
  useSendPaymentReminderMutation,
  useCalculatePaymentAmountQuery,
  useBulkProcessPaymentsMutation,
  useGetPaymentMethodsQuery,
  useExportPaymentHistoryMutation,
  useVerifyPaymentMutation,
  useGetPaymentAuditTrailQuery,
} = paymentsApi;
