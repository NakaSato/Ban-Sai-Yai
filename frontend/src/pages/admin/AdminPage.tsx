import React from 'react';
import { Typography, Paper, Box } from '@mui/material';

const AdminPage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Admin
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Admin page - Coming soon</Typography>
      </Paper>
    </Box>
  );
};

export default AdminPage;
