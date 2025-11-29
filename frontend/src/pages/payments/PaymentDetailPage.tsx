import React from 'react';
import { Typography, Paper, Box } from '@mui/material';

const PaymentDetailPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Payment Details
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Payment detail page - Coming soon</Typography>
      </Paper>
    </Box>
  );
};

export default PaymentDetailPage;
