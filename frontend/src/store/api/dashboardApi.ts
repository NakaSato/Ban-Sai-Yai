import { apiSlice } from './apiSlice';
import { DashboardStats, ChartData } from '@/types';

export const dashboardApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Get dashboard statistics
    getDashboardStats: builder.query<DashboardStats, { role?: string; period?: string }>({
      query: ({ role, period = 'month' }) => ({
        url: '/dashboard/stats',
        params: { role, period },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get member growth chart data
    getMemberGrowthChart: builder.query<ChartData, { period: string }>({
      query: ({ period }) => ({
        url: '/dashboard/charts/member-growth',
        params: { period },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get loan portfolio chart data
    getLoanPortfolioChart: builder.query<ChartData, { period: string }>({
      query: ({ period }) => ({
        url: '/dashboard/charts/loan-portfolio',
        params: { period },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get savings growth chart data
    getSavingsGrowthChart: builder.query<ChartData, { period: string }>({
      query: ({ period }) => ({
        url: '/dashboard/charts/savings-growth',
        params: { period },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get payment collection chart data
    getPaymentCollectionChart: builder.query<ChartData, { period: string }>({
      query: ({ period }) => ({
        url: '/dashboard/charts/payment-collection',
        params: { period },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get loan status distribution
    getLoanStatusDistribution: builder.query<ChartData, void>({
      query: () => '/dashboard/charts/loan-status-distribution',
      providesTags: ['Dashboard'],
    }),

    // Get savings account distribution
    getSavingsAccountDistribution: builder.query<ChartData, void>({
      query: () => '/dashboard/charts/savings-account-distribution',
      providesTags: ['Dashboard'],
    }),

    // Get payment status distribution
    getPaymentStatusDistribution: builder.query<ChartData, void>({
      query: () => '/dashboard/charts/payment-status-distribution',
      providesTags: ['Dashboard'],
    }),

    // Get recent activities
    getRecentActivities: builder.query<Array<{
      id: string;
      type: 'MEMBER' | 'LOAN' | 'SAVINGS' | 'PAYMENT';
      action: string;
      description: string;
      performedBy: string;
      timestamp: string;
      entityId: string;
      entityName: string;
    }>, { limit?: number }>({
      query: ({ limit = 10 }) => ({
        url: '/dashboard/recent-activities',
        params: { limit },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get upcoming tasks
    getUpcomingTasks: builder.query<Array<{
      id: string;
      type: 'LOAN_APPROVAL' | 'PAYMENT_DUE' | 'DOCUMENT_EXPIRY' | 'MEETING';
      title: string;
      description: string;
      dueDate: string;
      priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
      assignee?: string;
      entityId?: string;
    }>, { role?: string }>({
      query: ({ role }) => ({
        url: '/dashboard/upcoming-tasks',
        params: { role },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get financial summary
    getFinancialSummary: builder.query<{
      totalAssets: number;
      totalLiabilities: number;
      netWorth: number;
      totalRevenue: number;
      totalExpenses: number;
      netIncome: number;
      cashFlow: number;
      period: string;
      comparison: {
        assets: number;
        liabilities: number;
        revenue: number;
        expenses: number;
        income: number;
        cashFlow: number;
      };
    }, { period: string; compareWithPrevious?: boolean }>({
      query: ({ period, compareWithPrevious = true }) => ({
        url: '/dashboard/financial-summary',
        params: { period, compareWithPrevious },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get performance metrics
    getPerformanceMetrics: builder.query<{
      loanApprovalRate: number;
      loanDisbursementRate: number;
      paymentCollectionRate: number;
      memberRetentionRate: number;
      savingsGrowthRate: number;
      portfolioYield: number;
      operatingEfficiency: number;
      customerSatisfaction: number;
      period: string;
    }, { period: string }>({
      query: ({ period }) => ({
        url: '/dashboard/performance-metrics',
        params: { period },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get top performers
    getTopPerformers: builder.query<Array<{
      id: string;
      name: string;
      type: 'MEMBER' | 'OFFICER' | 'BRANCH';
      metric: string;
      value: number;
      rank: number;
      period: string;
    }>, { metric: string; period: string; limit?: number }>({
      query: ({ metric, period, limit = 10 }) => ({
        url: '/dashboard/top-performers',
        params: { metric, period, limit },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get risk indicators
    getRiskIndicators: builder.query<{
      overallRisk: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
      portfolioAtRisk: number;
      overdueRate: number;
      defaultRate: number;
      concentrationRisk: number;
      liquidityRisk: number;
      creditRisk: number;
      operationalRisk: number;
      marketRisk: number;
      alerts: Array<{
        id: string;
        type: 'WARNING' | 'ALERT' | 'CRITICAL';
        message: string;
        metric: string;
        threshold: number;
        currentValue: number;
        timestamp: string;
      }>;
    }, void>({
      query: () => '/dashboard/risk-indicators',
      providesTags: ['Dashboard'],
    }),

    // Get trends and forecasts
    getTrendsAndForecasts: builder.query<{
      membershipTrend: {
        current: number;
        projected: number;
        growthRate: number;
        data: Array<{ period: string; actual: number; projected: number }>;
      };
      loanPortfolioTrend: {
        current: number;
        projected: number;
        growthRate: number;
        data: Array<{ period: string; actual: number; projected: number }>;
      };
      savingsTrend: {
        current: number;
        projected: number;
        growthRate: number;
        data: Array<{ period: string; actual: number; projected: number }>;
      };
      revenueTrend: {
        current: number;
        projected: number;
        growthRate: number;
        data: Array<{ period: string; actual: number; projected: number }>;
      };
    }, { forecastPeriods?: number }>({
      query: ({ forecastPeriods = 12 }) => ({
        url: '/dashboard/trends-forecasts',
        params: { forecastPeriods },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get quick actions (role-based)
    getQuickActions: builder.query<Array<{
      id: string;
      title: string;
      description: string;
      icon: string;
      route: string;
      permission: string;
      priority: number;
    }>, { role: string }>({
      query: ({ role }) => ({
        url: '/dashboard/quick-actions',
        params: { role },
      }),
      providesTags: ['Dashboard'],
    }),

    // Get system health
    getSystemHealth: builder.query<{
      status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
      uptime: number;
      responseTime: number;
      database: 'ONLINE' | 'OFFLINE' | 'SLOW';
      backup: 'SUCCESS' | 'FAILED' | 'PENDING';
      security: 'SECURE' | 'WARNING' | 'COMPROMISED';
      storage: {
        used: number;
        total: number;
        percentage: number;
      };
      memory: {
        used: number;
        total: number;
        percentage: number;
      };
      lastUpdated: string;
    }, void>({
      query: () => '/dashboard/system-health',
      providesTags: ['Dashboard'],
    }),

    // Get notifications
    getNotifications: builder.query<Array<{
      id: string;
      type: 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';
      title: string;
      message: string;
      timestamp: string;
      read: boolean;
      actionUrl?: string;
      actionText?: string;
    }>, { unreadOnly?: boolean; limit?: number }>({
      query: ({ unreadOnly = false, limit = 20 }) => ({
        url: '/dashboard/notifications',
        params: { unreadOnly, limit },
      }),
      providesTags: ['Dashboard'],
    }),

    // Mark notification as read
    markNotificationAsRead: builder.mutation<void, { notificationId: string }>({
      query: ({ notificationId }) => ({
        url: `/dashboard/notifications/${notificationId}/read`,
        method: 'PATCH',
      }),
      invalidatesTags: ['Dashboard'],
    }),

    // Mark all notifications as read
    markAllNotificationsAsRead: builder.mutation<void, void>({
      query: () => ({
        url: '/dashboard/notifications/mark-all-read',
        method: 'PATCH',
      }),
      invalidatesTags: ['Dashboard'],
    }),
  }),
});

export const {
  useGetDashboardStatsQuery,
  useGetMemberGrowthChartQuery,
  useGetLoanPortfolioChartQuery,
  useGetSavingsGrowthChartQuery,
  useGetPaymentCollectionChartQuery,
  useGetLoanStatusDistributionQuery,
  useGetSavingsAccountDistributionQuery,
  useGetPaymentStatusDistributionQuery,
  useGetRecentActivitiesQuery,
  useGetUpcomingTasksQuery,
  useGetFinancialSummaryQuery,
  useGetPerformanceMetricsQuery,
  useGetTopPerformersQuery,
  useGetRiskIndicatorsQuery,
  useGetTrendsAndForecastsQuery,
  useGetQuickActionsQuery,
  useGetSystemHealthQuery,
  useGetNotificationsQuery,
  useMarkNotificationAsReadMutation,
  useMarkAllNotificationsAsReadMutation,
} = dashboardApi;
