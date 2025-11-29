import React from 'react';
import { Outlet } from 'react-router-dom';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard,
  People,
  AccountBalance,
  Savings,
  Receipt,
  Assessment,
  Settings,
  Person,
} from '@mui/icons-material';

import { useAppSelector, useAppDispatch } from '@/hooks/redux';
import {
  toggleSidebar,
  setSidebarCollapsed,
} from '@/store/slices/uiSlice';
import { UI } from '@/constants';

const drawerWidth = UI.SIDEBAR_WIDTH;

const menuItems = [
  {
    text: 'Dashboard',
    icon: <Dashboard />,
    path: '/dashboard',
    roles: ['PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER'],
  },
  {
    text: 'Members',
    icon: <People />,
    path: '/members',
    roles: ['PRESIDENT', 'SECRETARY', 'OFFICER'],
  },
  {
    text: 'Loans',
    icon: <AccountBalance />,
    path: '/loans',
    roles: ['PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER'],
  },
  {
    text: 'Savings',
    icon: <Savings />,
    path: '/savings',
    roles: ['PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER'],
  },
  {
    text: 'Payments',
    icon: <Receipt />,
    path: '/payments',
    roles: ['PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER'],
  },
  {
    text: 'Reports',
    icon: <Assessment />,
    path: '/reports',
    roles: ['PRESIDENT', 'SECRETARY', 'OFFICER'],
  },
  {
    text: 'Admin',
    icon: <Settings />,
    path: '/admin',
    roles: ['PRESIDENT', 'SECRETARY'],
  },
];

const Layout: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const dispatch = useAppDispatch();
  const { sidebarCollapsed } = useAppSelector((state) => state.ui);
  const { user } = useAppSelector((state) => state.auth);

  const handleDrawerToggle = () => {
    if (isMobile) {
      dispatch(toggleSidebar());
    } else {
      dispatch(setSidebarCollapsed(!sidebarCollapsed));
    }
  };

  const filteredMenuItems = menuItems.filter(item =>
    user?.role && item.roles.includes(user.role)
  );

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap component="div">
          Bansaiyai
        </Typography>
      </Toolbar>
      <List>
        {filteredMenuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              component="a"
              href={item.path}
              selected={window.location.pathname === item.path}
            >
              <ListItemIcon>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { md: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Financial Management System
          </Typography>
          <IconButton color="inherit">
            <Person />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Box
        component="nav"
        sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
      >
        <Drawer
          variant={isMobile ? 'temporary' : 'persistent'}
          open={isMobile ? !sidebarCollapsed : !sidebarCollapsed}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              ...(isMobile && {
                display: sidebarCollapsed ? 'none' : 'block',
              }),
              ...(!isMobile && {
                display: sidebarCollapsed ? 'none' : 'block',
              }),
            },
          }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: sidebarCollapsed ? 0 : 0 },
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default Layout;
