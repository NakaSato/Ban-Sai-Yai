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
import ErrorBoundary from "./components/ErrorBoundary";
import LoadingSpinner from "./components/LoadingSpinner";

// Components
import Layout from "./components/Layout";
import LoginPage from "./pages/auth/LoginPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "./pages/auth/ResetPasswordPage";
import DashboardPage from "./pages/dashboard/DashboardPage";
import MembersPage from "./pages/members/MembersPage";
import MemberDetailPage from "./pages/members/MemberDetailPage";
import LoansPage from "./pages/loans/LoansPage";
import LoanDetailPage from "./pages/loans/LoanDetailPage";
import LoanApplicationPage from "./pages/loans/LoanApplicationPage";
import SavingsPage from "./pages/savings/SavingsPage";
import SavingsDetailPage from "./pages/savings/SavingsDetailPage";
import PaymentsPage from "./pages/payments/PaymentsPage";
import PaymentDetailPage from "./pages/payments/PaymentDetailPage";
import ReportsPage from "./pages/reports/ReportsPage";
import AdminPage from "./pages/admin/AdminPage";
import ProfilePage from "./pages/profile/ProfilePage";

// Protected Route Component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading } = useAppSelector((state) => state.auth);

  if (isLoading) {
    return <LoadingSpinner message="Authenticating..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// Public Route Component (redirects to dashboard if authenticated)
const PublicRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

const AppContent = () => {
  const theme = useAppSelector((state) => state.ui.theme);
  const dispatch = store.dispatch;

  useEffect(() => {
    dispatch(loadUserFromStorage());
  }, [dispatch]);

  return (
    <ThemeProvider theme={getTheme(theme)}>
      <CssBaseline />
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <ErrorBoundary>
          <Router>
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
