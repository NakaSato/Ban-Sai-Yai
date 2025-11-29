import React from 'react';
import { Typography, Paper, Box } from '@mui/material';

const ProfilePage: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Profile
      </Typography>
      <Paper sx={{ p: 2 }}>
        <Typography>Profile page - Coming soon</Typography>
      </Paper>
    </Box>
  );
};

export default ProfilePage;
