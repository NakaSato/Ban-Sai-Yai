import React, { useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { Provider } from "react-redux";
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";

import { store } from "./store";
import { getTheme } from "./theme";
import { useAppSelector, useAppDispatch } from "./hooks/redux";
import { loadUserFromStorage } from "./store/slices/authSlice";
import { useTokenRefresh } from "./hooks/useTokenRefresh";
import ErrorBoundary from "./components/ErrorBoundary";
import LoadingSpinner from "./components/LoadingSpinner";

// Components
import Layout from "./components/Layout";

// Lazy-loaded route components
const LoginPage = React.lazy(() => import("./pages/auth/LoginPage"));
const ForgotPasswordPage = React.lazy(
  () => import("./pages/auth/ForgotPasswordPage")
);
const ResetPasswordPage = React.lazy(
  () => import("./pages/auth/ResetPasswordPage")
);
const DashboardPage = React.lazy(
  () => import("./pages/dashboard/DashboardPage")
);
const MembersPage = React.lazy(() => import("./pages/members/MembersPage"));
const MemberDetailPage = React.lazy(
  () => import("./pages/members/MemberDetailPage")
);
const LoansPage = React.lazy(() => import("./pages/loans/LoansPage"));
const LoanDetailPage = React.lazy(() => import("./pages/loans/LoanDetailPage"));
const LoanApplicationPage = React.lazy(
  () => import("./pages/loans/LoanApplicationPage")
);
const SavingsPage = React.lazy(() => import("./pages/savings/SavingsPage"));
const SavingsDetailPage = React.lazy(
  () => import("./pages/savings/SavingsDetailPage")
);
const PaymentsPage = React.lazy(() => import("./pages/payments/PaymentsPage"));
const PaymentDetailPage = React.lazy(
  () => import("./pages/payments/PaymentDetailPage")
);
const ReportsPage = React.lazy(() => import("./pages/reports/ReportsPage"));
const AdminPage = React.lazy(() => import("./pages/admin/AdminPage"));
const ProfilePage = React.lazy(() => import("./pages/profile/ProfilePage"));

// Protected Route Component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading, isInitialized } = useAppSelector(
    (state) => state.auth
  );

  if (!isInitialized || isLoading) {
    return <LoadingSpinner message="Authenticating..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// Public Route Component (redirects to dashboard if authenticated)
const PublicRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isInitialized } = useAppSelector(
    (state) => state.auth
  );

  if (!isInitialized) {
    return <LoadingSpinner fullScreen />;
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

const AppRoutes = () => {
  const isAuthenticated = useAppSelector((state) => state.auth.isAuthenticated);
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(loadUserFromStorage());
  }, [dispatch]);

  // Initialize token refresh hook with default configuration
  // Only runs when user is authenticated
  useTokenRefresh({
    refreshThresholdMinutes: 5,
    maxRetries: 3,
    enabled: isAuthenticated,
  });

  return (
    <React.Suspense
      fallback={<LoadingSpinner message="Loading..." fullScreen />}
    >
      <Routes>
        {/* Public Routes */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          path="/forgot-password"
          element={
            <PublicRoute>
              <ForgotPasswordPage />
            </PublicRoute>
          }
        />
        <Route
          path="/reset-password/:token"
          element={
            <PublicRoute>
              <ResetPasswordPage />
            </PublicRoute>
          }
        />

        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />

          {/* Member Routes */}
          <Route path="members" element={<MembersPage />} />
          <Route path="members/:id" element={<MemberDetailPage />} />

          {/* Loan Routes */}
          <Route path="loans" element={<LoansPage />} />
          <Route path="loans/apply" element={<LoanApplicationPage />} />
          <Route path="loans/:id" element={<LoanDetailPage />} />

          {/* Savings Routes */}
          <Route path="savings" element={<SavingsPage />} />
          <Route path="savings/:id" element={<SavingsDetailPage />} />

          {/* Payment Routes */}
          <Route path="payments" element={<PaymentsPage />} />
          <Route path="payments/:id" element={<PaymentDetailPage />} />

          {/* Reports Route */}
          <Route path="reports" element={<ReportsPage />} />

          {/* Admin Routes */}
          <Route path="admin" element={<AdminPage />} />

          {/* Profile Route */}
          <Route path="profile" element={<ProfilePage />} />
        </Route>

        {/* Catch all route */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </React.Suspense>
  );
};

const AppContent = () => {
  const theme = useAppSelector((state) => state.ui.theme);

  return (
    <ThemeProvider theme={getTheme(theme)}>
      <CssBaseline />
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <ErrorBoundary>
          <Router
            future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
          >
            <AppRoutes />
          </Router>
        </ErrorBoundary>
      </LocalizationProvider>
    </ThemeProvider>
  );
};

const App = () => {
  return (
    <ErrorBoundary>
      <Provider store={store}>
        <AppContent />
      </Provider>
    </ErrorBoundary>
  );
};

export default App;
