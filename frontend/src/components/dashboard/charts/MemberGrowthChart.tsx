import React from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Paper, Typography, Box, CircularProgress } from '@mui/material';
import { useGetMemberGrowthChartQuery } from '@/store/api/dashboardApi';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const MemberGrowthChart: React.FC = () => {
  const { data: chartData, isLoading, error } = useGetMemberGrowthChartQuery({ period: '6months' });

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: false,
        text: 'Member Growth',
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
        Member Growth
      </Typography>
      <Box height={300}>
        <Line options={options} data={chartData as any} />
      </Box>
    </Paper>
  );
};

export default MemberGrowthChart;
