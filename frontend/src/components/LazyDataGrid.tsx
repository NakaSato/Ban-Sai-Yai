import React, { lazy, Suspense } from 'react';
import { DataGridProps } from '@mui/x-data-grid';
import { Box, CircularProgress } from '@mui/material';
import LazyComponentWrapper from './LazyComponentWrapper';

// Lazy load the DataGrid component
const DataGrid = lazy(() => 
  import('@mui/x-data-grid').then(module => ({ default: module.DataGrid }))
);

/**
 * Lazy-loaded wrapper for Material-UI DataGrid component.
 * This reduces initial bundle size by loading the DataGrid library only when needed.
 */
const LazyDataGrid: React.FC<DataGridProps> = (props) => {
  return (
    <LazyComponentWrapper
      fallback={
        <Box
          display="flex"
          justifyContent="center"
          alignItems="center"
          minHeight={400}
        >
          <CircularProgress />
        </Box>
      }
    >
      <DataGrid {...props} />
    </LazyComponentWrapper>
  );
};

export default LazyDataGrid;
