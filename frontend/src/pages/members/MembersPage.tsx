import React from 'react';
import { Typography, Paper, Box } from '@mui/material';

const MembersPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Members
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Members management page - Coming soon</Typography>
      </Paper>
    </Box>
  );
};

export default MembersPage;
