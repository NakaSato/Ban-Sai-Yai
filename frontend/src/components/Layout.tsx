import React from "react";
import { Outlet } from "react-router-dom";
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  useTheme,
  useMediaQuery,
} from "@mui/material";
import { Menu as MenuIcon, Person } from "@mui/icons-material";

import { useAppSelector, useAppDispatch } from "@/hooks/redux";
import { toggleSidebar, setSidebarCollapsed } from "@/store/slices/uiSlice";
import Sidebar from "./Sidebar";

const Layout: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));
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

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar
        position="fixed"
        sx={{
          width: { lg: `calc(100% - 288px)` },
          ml: { lg: `288px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { lg: "none" } }}
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

      <Sidebar
        userRole={user?.role || "MEMBER"}
        userName={
          `${user?.firstName || ""} ${user?.lastName || ""}`.trim() || "User"
        }
        userPhoto={undefined}
        open={!sidebarCollapsed}
        onClose={handleDrawerToggle}
      />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { lg: `calc(100% - 288px)` },
          ml: { lg: "288px" },
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default Layout;
