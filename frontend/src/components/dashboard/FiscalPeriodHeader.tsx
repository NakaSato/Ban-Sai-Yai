import React, { useEffect } from 'react';
import { Box, Chip, Typography } from '@mui/material';
import { useGetFiscalPeriodQuery } from '@/store/api/dashboardApi';

interface FiscalPeriodHeaderProps {
  onStatusChange?: (status: 'OPEN' | 'CLOSED') => void;
}

const FiscalPeriodHeader: React.FC<FiscalPeriodHeaderProps> = ({ onStatusChange }) => {
  const { data: fiscalPeriod, isLoading } = useGetFiscalPeriodQuery();

  useEffect(() => {
    if (fiscalPeriod && onStatusChange) {
      onStatusChange(fiscalPeriod.status);
    }
  }, [fiscalPeriod, onStatusChange]);

  if (isLoading || !fiscalPeriod) {
    return null;
  }

  const isOpen = fiscalPeriod.status === 'OPEN';

  return (
    <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
      <Typography variant="body2" color="text.secondary">
        Fiscal Period:
      </Typography>
      <Chip
        label={`${fiscalPeriod.period} - ${fiscalPeriod.status}`}
        color={isOpen ? 'success' : 'error'}
        size="small"
        sx={{
          fontWeight: 'bold',
          backgroundColor: isOpen ? '#4caf50' : '#f44336',
          color: 'white',
        }}
      />
    </Box>
  );
};

export default FiscalPeriodHeader;
