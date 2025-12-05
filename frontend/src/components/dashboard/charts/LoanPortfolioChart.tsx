import React from 'react';
import { Doughnut } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Paper, Typography, Box, CircularProgress } from '@mui/material';
import { useGetLoanPortfolioChartQuery } from '@/store/api/dashboardApi';

ChartJS.register(ArcElement, Tooltip, Legend);

const LoanPortfolioChart: React.FC = () => {
  const { data: chartData, isLoading, error } = useGetLoanPortfolioChartQuery({ period: 'current' });

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: 'right' as const,
      },
    },
  };

  if (isLoading) {
    return (
      <Paper sx={{ p: 2, height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <CircularProgress />
      </Paper>
    );
  }

  if (error || !chartData) {
    return (
      <Paper sx={{ p: 2, height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Typography color="error">Failed to load chart data</Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h6" gutterBottom>
        Loan Portfolio
      </Typography>
      <Box height={300} display="flex" justifyContent="center">
        <Doughnut data={chartData as any} options={options} />
      </Box>
    </Paper>
  );
};

export default LoanPortfolioChart;
