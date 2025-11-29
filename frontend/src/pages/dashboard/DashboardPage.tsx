import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Paper,
} from '@mui/material';
import {
  AccountBalance,
  People,
  Savings,
  TrendingUp,
} from '@mui/icons-material';

const DashboardPage: React.FC = () => {
  // Mock data - this will come from API
  const stats = [
    {
      title: 'Total Members',
      value: '1,234',
      icon: <People />,
      color: '#1976d2',
    },
    {
      title: 'Active Loans',
      value: '456',
      icon: <AccountBalance />,
      color: '#388e3c',
    },
    {
      title: 'Total Savings',
      value: '₱2,345,678',
      icon: <Savings />,
      color: '#f57c00',
    },
    {
      title: 'Monthly Revenue',
      value: '₱123,456',
      icon: <TrendingUp />,
      color: '#7b1fa2',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      
      <Box display="flex" flexWrap="wrap" gap={3}>
        {stats.map((stat, index) => (
          <Box key={index} flex="1" minWidth="250px">
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Box
                    sx={{
                      backgroundColor: stat.color,
                      color: 'white',
                      borderRadius: 1,
                      p: 1,
                      mr: 2,
                      display: 'flex',
                    }}
                  >
                    {stat.icon}
                  </Box>
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      {stat.title}
                    </Typography>
                    <Typography variant="h5">
                      {stat.value}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Box>
        ))}
      </Box>

      <Box display="flex" gap={3} mt={3}>
        <Box flex="2" minWidth="300px">
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activity
            </Typography>
            <Typography color="textSecondary">
              Chart will be displayed here
            </Typography>
          </Paper>
        </Box>

        <Box flex="1" minWidth="200px">
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Quick Actions
            </Typography>
            <Typography color="textSecondary">
              Quick action buttons will be displayed here
            </Typography>
          </Paper>
        </Box>
      </Box>
    </Box>
  );
};

export default DashboardPage;
