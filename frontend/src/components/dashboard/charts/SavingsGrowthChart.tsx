import React from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Paper, Typography, Box, CircularProgress } from '@mui/material';
import { useGetSavingsGrowthChartQuery } from '@/store/api/dashboardApi';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const SavingsGrowthChart: React.FC = () => {
  const { data: chartData, isLoading, error } = useGetSavingsGrowthChartQuery({ period: '6months' });

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: false,
        text: 'Savings Growth',
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
        Savings Growth
      </Typography>
      <Box height={300}>
        <Bar options={options} data={chartData as any} />
      </Box>
    </Paper>
  );
};

export default SavingsGrowthChart;
