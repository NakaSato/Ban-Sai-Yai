import { apiSlice } from './apiSlice';
import { LoginRequest, LoginResponse, User } from '@/types';

export const authApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Login user
    login: builder.mutation<LoginResponse, LoginRequest>({
      query: (credentials) => ({
        url: '/auth/login',
        method: 'POST',
        body: credentials,
      }),
    }),

    // Logout user
    logout: builder.mutation<void, void>({
      query: () => ({
        url: '/auth/logout',
        method: 'POST',
      }),
    }),

    // Refresh token
    refreshToken: builder.mutation<{ token: string }, { token: string }>({
      query: ({ token }) => ({
        url: '/auth/refresh',
        method: 'POST',
        body: { token },
      }),
    }),

    // Get current user
    getCurrentUser: builder.query<User, void>({
      query: () => '/auth/me',
      providesTags: ['User'],
    }),

    // Change password
    changePassword: builder.mutation<void, {
      currentPassword: string;
      newPassword: string;
      confirmPassword: string;
    }>({
      query: (passwordData) => ({
        url: '/auth/change-password',
        method: 'POST',
        body: passwordData,
      }),
    }),

    // Update profile
    updateProfile: builder.mutation<User, Partial<User>>({
      query: (profileData) => ({
        url: '/auth/profile',
        method: 'PUT',
        body: profileData,
      }),
      invalidatesTags: ['User'],
    }),

    // Request password reset
    requestPasswordReset: builder.mutation<void, { email: string }>({
      query: ({ email }) => ({
        url: '/auth/forgot-password',
        method: 'POST',
        body: { email },
      }),
    }),

    // Reset password
    resetPassword: builder.mutation<void, {
      token: string;
      newPassword: string;
      confirmPassword: string;
    }>({
      query: (resetData) => ({
        url: '/auth/reset-password',
        method: 'POST',
        body: resetData,
      }),
    }),

    // Verify email
    verifyEmail: builder.mutation<void, { token: string }>({
      query: ({ token }) => ({
        url: '/auth/verify-email',
        method: 'POST',
        body: { token },
      }),
    }),

    // Resend verification email
    resendVerificationEmail: builder.mutation<void, { email: string }>({
      query: ({ email }) => ({
        url: '/auth/resend-verification',
        method: 'POST',
        body: { email },
      }),
    }),

    // Check if username is available
    checkUsernameAvailability: builder.query<{ available: boolean }, { username: string }>({
      query: ({ username }) => `/auth/check-username/${username}`,
    }),

    // Check if email is available
    checkEmailAvailability: builder.query<{ available: boolean }, { email: string }>({
      query: ({ email }) => `/auth/check-email/${email}`,
    }),

    // Get user permissions
    getUserPermissions: builder.query<string[], { userId?: string }>({
      query: ({ userId }) => ({
        url: '/auth/permissions',
        params: userId ? { userId } : {},
      }),
    }),

    // Update user preferences
    updateUserPreferences: builder.mutation<void, {
      theme?: string;
      language?: string;
      notifications?: Record<string, boolean>;
      dashboardLayout?: Record<string, any>;
    }>({
      query: (preferences) => ({
        url: '/auth/preferences',
        method: 'PUT',
        body: preferences,
      }),
    }),

    // Get user preferences
    getUserPreferences: builder.query<{
      theme: string;
      language: string;
      notifications: Record<string, boolean>;
      dashboardLayout: Record<string, any>;
    }, void>({
      query: () => '/auth/preferences',
    }),

    // Enable two-factor authentication
    enableTwoFactor: builder.mutation<{
      secret: string;
      qrCode: string;
      backupCodes: string[];
    }, void>({
      query: () => ({
        url: '/auth/2fa/enable',
        method: 'POST',
      }),
    }),

    // Disable two-factor authentication
    disableTwoFactor: builder.mutation<void, {
      code: string;
    }>({
      query: ({ code }) => ({
        url: '/auth/2fa/disable',
        method: 'POST',
        body: { code },
      }),
    }),

    // Verify two-factor authentication
    verifyTwoFactor: builder.mutation<{ verified: boolean }, {
      code: string;
    }>({
      query: ({ code }) => ({
        url: '/auth/2fa/verify',
        method: 'POST',
        body: { code },
      }),
    }),

    // Get user activity log
    getUserActivity: builder.query<Array<{
      id: string;
      action: string;
      timestamp: string;
      ipAddress: string;
      userAgent: string;
      location?: string;
      success: boolean;
    }>, {
      page?: number;
      size?: number;
      startDate?: string;
      endDate?: string;
    }>({
      query: (params) => ({
        url: '/auth/activity',
        params,
      }),
    }),

    // Revoke active sessions
    revokeSessions: builder.mutation<void, {
      sessionIds?: string[];
      revokeAll?: boolean;
    }>({
      query: (data) => ({
        url: '/auth/sessions/revoke',
        method: 'POST',
        body: data,
      }),
    }),

    // Get active sessions
    getActiveSessions: builder.query<Array<{
      id: string;
      device: string;
      browser: string;
      os: string;
      ipAddress: string;
      location?: string;
      lastActivity: string;
      isCurrent: boolean;
    }>, void>({
      query: () => '/auth/sessions',
    }),
  }),
});

export const {
  useLoginMutation,
  useLogoutMutation,
  useRefreshTokenMutation,
  useGetCurrentUserQuery,
  useChangePasswordMutation,
  useUpdateProfileMutation,
  useRequestPasswordResetMutation,
  useResetPasswordMutation,
  useVerifyEmailMutation,
  useResendVerificationEmailMutation,
  useCheckUsernameAvailabilityQuery,
  useCheckEmailAvailabilityQuery,
  useGetUserPermissionsQuery,
  useUpdateUserPreferencesMutation,
  useGetUserPreferencesQuery,
  useEnableTwoFactorMutation,
  useDisableTwoFactorMutation,
  useVerifyTwoFactorMutation,
  useGetUserActivityQuery,
  useRevokeSessionsMutation,
  useGetActiveSessionsQuery,
} = authApi;
