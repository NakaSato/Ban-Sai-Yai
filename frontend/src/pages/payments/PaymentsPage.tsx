import React from 'react';
import { Typography, Paper, Box } from '@mui/material';

const PaymentsPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Payments
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Payments management page - Coming soon</Typography>
      </Paper>
    </Box>
  );
};

export default PaymentsPage;
