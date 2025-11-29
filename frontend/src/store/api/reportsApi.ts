import { apiSlice } from './apiSlice';

export const reportsApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Generate balance sheet report
    generateBalanceSheet: builder.mutation<Blob, {
      asOfDate: string;
      format: 'pdf' | 'excel';
      includeComparisons?: boolean;
      comparisonDate?: string;
    }>({
      query: (reportData) => ({
        url: '/reports/balance-sheet',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate profit and loss statement
    generateProfitLossStatement: builder.mutation<Blob, {
      startDate: string;
      endDate: string;
      format: 'pdf' | 'excel';
      includeComparisons?: boolean;
      comparisonStartDate?: string;
      comparisonEndDate?: string;
    }>({
      query: (reportData) => ({
        url: '/reports/profit-loss',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate loan portfolio report
    generateLoanPortfolioReport: builder.mutation<Blob, {
      asOfDate: string;
      format: 'pdf' | 'excel';
      includeAging?: boolean;
      includeRiskAnalysis?: boolean;
      loanType?: string;
      loanStatus?: string;
    }>({
      query: (reportData) => ({
        url: '/reports/loan-portfolio',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate member statements report
    generateMemberStatements: builder.mutation<Blob, {
      memberId?: string;
      startDate: string;
      endDate: string;
      format: 'pdf' | 'excel';
      includeLoans?: boolean;
      includeSavings?: boolean;
      includePayments?: boolean;
    }>({
      query: (reportData) => ({
        url: '/reports/member-statements',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate cash flow report
    generateCashFlowReport: builder.mutation<Blob, {
      startDate: string;
      endDate: string;
      format: 'pdf' | 'excel';
      includeProjections?: boolean;
      projectionMonths?: number;
    }>({
      query: (reportData) => ({
        url: '/reports/cash-flow',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate aging report
    generateAgingReport: builder.mutation<Blob, {
      asOfDate: string;
      format: 'pdf' | 'excel';
      includePaid?: boolean;
      agingBuckets?: number[];
    }>({
      query: (reportData) => ({
        url: '/reports/aging',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate dividend report
    generateDividendReport: builder.mutation<Blob, {
      year: number;
      format: 'pdf' | 'excel';
      includeCalculations?: boolean;
      memberCategory?: string;
    }>({
      query: (reportData) => ({
        url: '/reports/dividend',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate audit trail report
    generateAuditTrailReport: builder.mutation<Blob, {
      startDate: string;
      endDate: string;
      format: 'pdf' | 'excel';
      entityType?: string;
      actionType?: string;
      userId?: string;
    }>({
      query: (reportData) => ({
        url: '/reports/audit-trail',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate risk assessment report
    generateRiskAssessmentReport: builder.mutation<Blob, {
      asOfDate: string;
      format: 'pdf' | 'excel';
      includeRecommendations?: boolean;
      riskCategories?: string[];
    }>({
      query: (reportData) => ({
        url: '/reports/risk-assessment',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Generate custom report
    generateCustomReport: builder.mutation<Blob, {
      reportType: string;
      parameters: Record<string, any>;
      format: 'pdf' | 'excel' | 'csv';
    }>({
      query: (reportData) => ({
        url: '/reports/custom',
        method: 'POST',
        body: reportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Get available report templates
    getReportTemplates: builder.query<Array<{
      id: string;
      name: string;
      description: string;
      category: string;
      parameters: Array<{
        name: string;
        type: string;
        required: boolean;
        defaultValue?: any;
        options?: string[];
      }>;
      formats: string[];
      permissions: string[];
    }>, { category?: string }>({
      query: ({ category }) => ({
        url: '/reports/templates',
        params: { category },
      }),
    }),

    // Get report history
    getReportHistory: builder.query<{
      reports: Array<{
        id: string;
        name: string;
        type: string;
        generatedBy: string;
        generatedAt: string;
        parameters: Record<string, any>;
        format: string;
        fileSize: number;
        downloadUrl: string;
        expiresAt?: string;
      }>;
      total: number;
      page: number;
      size: number;
    }, {
      page?: number;
      size?: number;
      reportType?: string;
      startDate?: string;
      endDate?: string;
      generatedBy?: string;
    }>({
      query: (params) => ({
        url: '/reports/history',
        params,
      }),
    }),

    // Download report from history
    downloadReport: builder.query<Blob, string>({
      query: (reportId) => ({
        url: `/reports/history/${reportId}/download`,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Delete report from history
    deleteReport: builder.mutation<void, string>({
      query: (reportId) => ({
        url: `/reports/history/${reportId}`,
        method: 'DELETE',
      }),
    }),

    // Schedule report generation
    scheduleReport: builder.mutation<{
      id: string;
      name: string;
      reportType: string;
      schedule: string;
      parameters: Record<string, any>;
      recipients: string[];
      format: string;
      isActive: boolean;
    }, {
      name: string;
      reportType: string;
      schedule: string;
      parameters: Record<string, any>;
      recipients: string[];
      format: string;
    }>({
      query: (scheduleData) => ({
        url: '/reports/schedule',
        method: 'POST',
        body: scheduleData,
      }),
    }),

    // Get scheduled reports
    getScheduledReports: builder.query<Array<{
      id: string;
      name: string;
      reportType: string;
      schedule: string;
      parameters: Record<string, any>;
      recipients: string[];
      format: string;
      isActive: boolean;
      lastRun?: string;
      nextRun?: string;
      createdBy: string;
      createdAt: string;
    }>, void>({
      query: () => '/reports/schedule',
    }),

    // Update scheduled report
    updateScheduledReport: builder.mutation<void, {
      id: string;
      updates: Partial<{
        name: string;
        schedule: string;
        parameters: Record<string, any>;
        recipients: string[];
        format: string;
        isActive: boolean;
      }>;
    }>({
      query: ({ id, updates }) => ({
        url: `/reports/schedule/${id}`,
        method: 'PUT',
        body: updates,
      }),
    }),

    // Delete scheduled report
    deleteScheduledReport: builder.mutation<void, string>({
      query: (id) => ({
        url: `/reports/schedule/${id}`,
        method: 'DELETE',
      }),
    }),

    // Run scheduled report manually
    runScheduledReport: builder.mutation<void, string>({
      query: (id) => ({
        url: `/reports/schedule/${id}/run`,
        method: 'POST',
      }),
    }),

    // Get report preview
    getReportPreview: builder.query<{
      columns: Array<{
        name: string;
        type: string;
        label: string;
      }>;
      data: Array<Record<string, any>>;
      totalCount: number;
    }, {
      reportType: string;
      parameters: Record<string, any>;
      limit?: number;
    }>({
      query: ({ reportType, parameters, limit = 100 }) => ({
        url: '/reports/preview',
        method: 'POST',
        body: { reportType, parameters, limit },
      }),
    }),

    // Export report data
    exportReportData: builder.mutation<Blob, {
      reportType: string;
      parameters: Record<string, any>;
      format: 'excel' | 'csv' | 'json';
      filters?: Record<string, any>;
    }>({
      query: (exportData) => ({
        url: '/reports/export',
        method: 'POST',
        body: exportData,
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    // Get report analytics
    getReportAnalytics: builder.query<{
      mostGenerated: Array<{
        reportType: string;
        count: number;
        lastGenerated: string;
      }>;
      generationTrends: Array<{
        date: string;
        count: number;
      }>;
      userActivity: Array<{
        userId: string;
        userName: string;
        reportCount: number;
        lastGenerated: string;
      }>;
      averageGenerationTime: number;
      totalReportsGenerated: number;
    }, {
      startDate?: string;
      endDate?: string;
    }>({
      query: ({ startDate, endDate }) => ({
        url: '/reports/analytics',
        params: { startDate, endDate },
      }),
    }),

    // Validate report parameters
    validateReportParameters: builder.mutation<{
      isValid: boolean;
      errors: string[];
      warnings: string[];
    }, {
      reportType: string;
      parameters: Record<string, any>;
    }>({
      query: (validationData) => ({
        url: '/reports/validate',
        method: 'POST',
        body: validationData,
      }),
    }),
  }),
});

export const {
  useGenerateBalanceSheetMutation,
  useGenerateProfitLossStatementMutation,
  useGenerateLoanPortfolioReportMutation,
  useGenerateMemberStatementsMutation,
  useGenerateCashFlowReportMutation,
  useGenerateAgingReportMutation,
  useGenerateDividendReportMutation,
  useGenerateAuditTrailReportMutation,
  useGenerateRiskAssessmentReportMutation,
  useGenerateCustomReportMutation,
  useGetReportTemplatesQuery,
  useGetReportHistoryQuery,
  useDownloadReportQuery,
  useDeleteReportMutation,
  useScheduleReportMutation,
  useGetScheduledReportsQuery,
  useUpdateScheduledReportMutation,
  useDeleteScheduledReportMutation,
  useRunScheduledReportMutation,
  useGetReportPreviewQuery,
  useExportReportDataMutation,
  useGetReportAnalyticsQuery,
  useValidateReportParametersMutation,
} = reportsApi;
