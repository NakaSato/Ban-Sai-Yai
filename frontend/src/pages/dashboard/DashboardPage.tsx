import React, { useState, lazy } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Paper,
  CircularProgress,
  Button,
  Grid,
} from '@mui/material';
import {
  AccountBalance,
  People,
  Savings,
  TrendingUp,
  Help,
} from '@mui/icons-material';
import * as Icons from '@mui/icons-material';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { RootState } from '@/store';
import { useGetDashboardStatsQuery, useGetQuickActionsQuery } from '@/store/api/dashboardApi';

import StatCard from '@/components/dashboard/StatCard';
import RecentActivityTable from '@/components/dashboard/RecentActivityTable';
import FiscalPeriodHeader from '@/components/dashboard/FiscalPeriodHeader';
import OmniSearchBar from '@/components/dashboard/OmniSearchBar';
import LazyComponentWrapper from '@/components/LazyComponentWrapper';
import LoadingSpinner from '@/components/LoadingSpinner';

// Lazy load heavy chart components
const MemberGrowthChart = lazy(() => import('@/components/dashboard/charts/MemberGrowthChart'));
const LoanPortfolioChart = lazy(() => import('@/components/dashboard/charts/LoanPortfolioChart'));
const SavingsGrowthChart = lazy(() => import('@/components/dashboard/charts/SavingsGrowthChart'));

const DashboardPage: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  const { data: dashboardStats, isLoading: isLoadingStats } = useGetDashboardStatsQuery({});
  const { data: quickActions, isLoading: isLoadingActions } = useGetQuickActionsQuery(
    { role: user?.role || '' },
    { skip: !user?.role }
  );
  const navigate = useNavigate();
  const [fiscalPeriodStatus, setFiscalPeriodStatus] = useState<'OPEN' | 'CLOSED'>('OPEN');

  const isLoading = isLoadingStats;

  const stats = [
    {
      title: 'Total Members',
      value: dashboardStats?.totalMembers?.toLocaleString() || '0',
      icon: <People />,
      color: '#1976d2',
    },
    {
      title: 'Active Loans',
      value: dashboardStats?.activeLoans?.toLocaleString() || '0',
      icon: <AccountBalance />,
      color: '#388e3c',
    },
    {
      title: 'Total Savings',
      value: `₱${dashboardStats?.totalSavings?.toLocaleString() || '0.00'}`,
      icon: <Savings />,
      color: '#f57c00',
    },
    {
      title: 'Monthly Revenue',
      value: `₱${dashboardStats?.monthlyRevenue?.toLocaleString() || '0.00'}`,
      icon: <TrendingUp />,
      color: '#7b1fa2',
    },
  ];

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4">
          Dashboard
        </Typography>
        <OmniSearchBar />
      </Box>

      <FiscalPeriodHeader onStatusChange={setFiscalPeriodStatus} />
      
      <Box display="flex" flexWrap="wrap" gap={3}>
        {stats.map((stat, index) => (
          <Box key={index} flex="1" minWidth="250px">
            <StatCard
              title={stat.title}
              value={stat.value}
              icon={stat.icon}
              color={stat.color}
            />
          </Box>
        ))}
      </Box>

      <Box display="flex" gap={3} mt={3}>
        <Box flex="2" minWidth="300px">
          <RecentActivityTable />
        </Box>

        <Box flex="1" minWidth="200px">
          <Paper sx={{ p: 2, height: 400, overflow: 'auto' }}>
            <Typography variant="h6" gutterBottom>
              Quick Actions
            </Typography>
            <Grid container spacing={2}>
              {quickActions?.map((action) => {
                const IconComponent = (Icons as any)[action.icon] || Help;
                return (
                  <Grid item xs={12} sm={6} md={12} key={action.id}>
                    <Button
                      variant="outlined"
                      fullWidth
                      startIcon={<IconComponent />}
                      onClick={() => navigate(action.route)}
                      sx={{ justifyContent: 'flex-start', textAlign: 'left', p: 1.5 }}
                    >
                      <Box>
                        <Typography variant="subtitle2">{action.title}</Typography>
                        <Typography variant="caption" color="textSecondary" display="block">
                          {action.description}
                        </Typography>
                      </Box>
                    </Button>
                  </Grid>
                );
              })}
              {(!quickActions || quickActions.length === 0) && (
                <Grid item xs={12}>
                  <Typography color="textSecondary" align="center">
                    No quick actions available.
                  </Typography>
                </Grid>
              )}
            </Grid>
          </Paper>
        </Box>
      </Box>

      <Box display="flex" flexWrap="wrap" gap={3} mt={3}>
        <Box flex="1" minWidth="300px">
          <LazyComponentWrapper fallback={<LoadingSpinner message="Loading chart..." />}>
            <MemberGrowthChart />
          </LazyComponentWrapper>
        </Box>
        <Box flex="1" minWidth="300px">
          <LazyComponentWrapper fallback={<LoadingSpinner message="Loading chart..." />}>
            <LoanPortfolioChart />
          </LazyComponentWrapper>
        </Box>
        <Box flex="1" minWidth="300px">
          <LazyComponentWrapper fallback={<LoadingSpinner message="Loading chart..." />}>
            <SavingsGrowthChart />
          </LazyComponentWrapper>
        </Box>
      </Box>
    </Box>
  );
};

export default DashboardPage;
