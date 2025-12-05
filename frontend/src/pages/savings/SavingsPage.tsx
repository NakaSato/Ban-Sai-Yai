import React from 'react';
import { Typography, Paper, Box } from '@mui/material';
// import LazyDataGrid from '@/components/LazyDataGrid';

/**
 * Savings management page
 * 
 * When implementing the savings table, use LazyDataGrid instead of DataGrid:
 * 
 * Example:
 * import LazyDataGrid from '@/components/LazyDataGrid';
 * 
 * <LazyDataGrid
 *   rows={savings}
 *   columns={columns}
 *   ...other props
 * />
 * 
 * This ensures the DataGrid library is lazy-loaded, reducing initial bundle size.
 */
const SavingsPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Savings
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Savings management page - Coming soon</Typography>
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          Note: When implementing the savings table, use LazyDataGrid for optimal performance.
        </Typography>
      </Paper>
    </Box>
  );
};

export default SavingsPage;
