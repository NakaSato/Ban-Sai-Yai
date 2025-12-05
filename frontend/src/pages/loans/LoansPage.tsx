import React from 'react';
import { Typography, Paper, Box } from '@mui/material';
// import LazyDataGrid from '@/components/LazyDataGrid';
// import LazyDatePicker from '@/components/LazyDatePicker';

/**
 * Loans management page
 * 
 * When implementing the loans table and date filters, use lazy-loaded components:
 * 
 * Example:
 * import LazyDataGrid from '@/components/LazyDataGrid';
 * import LazyDatePicker from '@/components/LazyDatePicker';
 * 
 * <LazyDataGrid rows={loans} columns={columns} ... />
 * <LazyDatePicker label="Filter by date" ... />
 * 
 * This ensures heavy Material-UI components are lazy-loaded, reducing initial bundle size.
 */
const LoansPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Loans
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Loans management page - Coming soon</Typography>
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          Note: When implementing, use LazyDataGrid and LazyDatePicker for optimal performance.
        </Typography>
      </Paper>
    </Box>
  );
};

export default LoansPage;
