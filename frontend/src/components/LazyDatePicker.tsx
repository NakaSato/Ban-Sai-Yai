import React, { lazy } from 'react';
import { DatePickerProps } from '@mui/x-date-pickers';
import { Box, CircularProgress } from '@mui/material';
import LazyComponentWrapper from './LazyComponentWrapper';

// Lazy load the DatePicker component
const DatePicker = lazy(() =>
  import('@mui/x-date-pickers').then(module => ({ default: module.DatePicker }))
);

/**
 * Lazy-loaded wrapper for Material-UI DatePicker component.
 * This reduces initial bundle size by loading the DatePicker library only when needed.
 */
const LazyDatePicker: React.FC<DatePickerProps<any>> = (props) => {
  return (
    <LazyComponentWrapper
      fallback={
        <Box
          display="flex"
          justifyContent="center"
          alignItems="center"
          minHeight={56}
        >
          <CircularProgress size={24} />
        </Box>
      }
    >
      <DatePicker {...props} />
    </LazyComponentWrapper>
  );
};

export default LazyDatePicker;
