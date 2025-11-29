import React from 'react';
import { Typography, Paper, Box } from '@mui/material';

const LoanApplicationPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Loan Application
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Loan application page - Coming soon</Typography>
      </Paper>
    </Box>
  );
};

export default LoanApplicationPage;
