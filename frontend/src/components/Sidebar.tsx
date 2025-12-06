"use client";
import React from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import {
  People,
  AttachMoney,
  Description,
  Settings,
  Logout,
  Home,
  BarChart,
  Stars,
  Dashboard,
  AccountBalance,
} from "@mui/icons-material";
import {
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Avatar,
  Divider,
  Button,
  useTheme,
  alpha,
} from "@mui/material";

interface SidebarProps {
  userRole: string;
  userName: string;
  userPhoto?: string;
  open?: boolean;
  onClose?: () => void;
}

const navItems = {
  officer: [
    { icon: Dashboard, label: "Dashboard", href: "/dashboard" },
    { icon: People, label: "Members", href: "/members" },
    { icon: AttachMoney, label: "Loans", href: "/loans" },
  ],
  secretary: [
    { icon: Dashboard, label: "Dashboard", href: "/dashboard" },
    { icon: Description, label: "Accounting", href: "/savings" },
    { icon: BarChart, label: "Reports", href: "/reports" },
  ],
  president: [
    { icon: Dashboard, label: "Dashboard", href: "/dashboard" },
    { icon: BarChart, label: "Analytics", href: "/dashboard" },
    { icon: AccountBalance, label: "Portfolio", href: "/dashboard" },
    { icon: People, label: "Members", href: "/members" },
  ],
  member: [
    { icon: Dashboard, label: "Dashboard", href: "/dashboard" },
    { icon: AttachMoney, label: "My Loans", href: "/loans" },
  ],
};

const getRoleColor = (role: string) => {
  const colors = {
    officer: "#1976d2", // blue
    secretary: "#7c4dff", // purple
    president: "#ff6f00", // amber
    member: "#388e3c", // green
  };
  return colors[role as keyof typeof colors] || colors.officer;
};

const getRoleGradient = (role: string) => {
  const gradients = {
    officer: "linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)",
    secretary: "linear-gradient(135deg, #7c4dff 0%, #b388ff 100%)",
    president: "linear-gradient(135deg, #ff6f00 0%, #ffa726 100%)",
    member: "linear-gradient(135deg, #388e3c 0%, #66bb6a 100%)",
  };
  return gradients[role as keyof typeof gradients] || gradients.officer;
};

export default function Sidebar({
  userRole,
  userName,
  userPhoto,
  open = true,
  onClose,
}: SidebarProps) {
  const theme = useTheme();
  const navigate = useNavigate();
  // Convert uppercase enum to lowercase for navItems lookup
  const normalizedRole = userRole.toLowerCase();
  const items =
    navItems[normalizedRole as keyof typeof navItems] || navItems.member;
  const roleLabel =
    userRole.charAt(0).toUpperCase() + userRole.slice(1).toLowerCase();

  const handleNavigation = (href: string) => {
    navigate(href);
    if (onClose) {
      onClose();
    }
  };

  const handleLogout = () => {
    // Add logout logic here
    console.log("Logout clicked");
  };

  const handleSettings = () => {
    navigate("/profile");
  };

  const drawerContent = (
    <Box
      sx={{
        height: "100vh",
        display: "flex",
        flexDirection: "column",
        bgcolor: "background.paper",
      }}
    >
      {/* Brand Header */}
      <Box sx={{ p: 3, borderBottom: 1, borderColor: "divider" }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: 40,
              height: 40,
              borderRadius: 2,
              background: getRoleGradient(normalizedRole),
            }}
          >
            <Stars sx={{ color: "white", fontSize: 24 }} />
          </Box>
          <Box>
            <Typography
              variant="h6"
              sx={{ fontWeight: "bold", color: "text.primary" }}
            >
              Ban Sai Yai
            </Typography>
            <Typography variant="caption" sx={{ color: "text.secondary" }}>
              Savings Group
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* User Profile Card */}
      <Box
        sx={{
          p: 2,
          mx: 2,
          mt: 2,
          borderRadius: 2,
          bgcolor: alpha(theme.palette.primary.main, 0.04),
          border: 1,
          borderColor: "divider",
        }}
      >
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Avatar
            src={userPhoto}
            sx={{
              width: 48,
              height: 48,
              bgcolor: getRoleColor(normalizedRole),
              fontWeight: "bold",
              fontSize: "1.2rem",
            }}
          >
            {userName.charAt(0).toUpperCase()}
          </Avatar>
          <Box sx={{ minWidth: 0, flex: 1 }}>
            <Typography
              variant="subtitle2"
              sx={{ fontWeight: 600, color: "text.primary" }}
              noWrap
            >
              {userName}
            </Typography>
            <Typography variant="caption" sx={{ color: "text.secondary" }}>
              {roleLabel}
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* Navigation Menu */}
      <Box sx={{ flex: 1, p: 2 }}>
        <List sx={{ py: 0 }}>
          {items.map((item) => {
            const Icon = item.icon;
            return (
              <ListItem key={item.label} disablePadding sx={{ mb: 0.5 }}>
                <ListItemButton
                  onClick={() => handleNavigation(item.href)}
                  sx={{
                    borderRadius: 1,
                    py: 1,
                    px: 2,
                    color: "text.primary",
                    "&:hover": {
                      bgcolor: alpha(theme.palette.primary.main, 0.08),
                    },
                    "&.Mui-selected": {
                      bgcolor: alpha(theme.palette.primary.main, 0.12),
                      color: "primary.main",
                      "&:hover": {
                        bgcolor: alpha(theme.palette.primary.main, 0.16),
                      },
                    },
                  }}
                >
                  <ListItemIcon sx={{ minWidth: 40, color: "inherit" }}>
                    <Icon fontSize="small" />
                  </ListItemIcon>
                  <ListItemText
                    primary={item.label}
                    primaryTypographyProps={{
                      fontSize: "0.875rem",
                      fontWeight: 500,
                    }}
                  />
                </ListItemButton>
              </ListItem>
            );
          })}
        </List>
      </Box>

      {/* Settings & Logout */}
      <Box sx={{ p: 2, borderTop: 1, borderColor: "divider" }}>
        <List sx={{ py: 0 }}>
          <ListItem disablePadding sx={{ mb: 0.5 }}>
            <ListItemButton
              onClick={handleSettings}
              sx={{
                borderRadius: 1,
                py: 1,
                px: 2,
                color: "text.primary",
                "&:hover": {
                  bgcolor: alpha(theme.palette.primary.main, 0.08),
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: "inherit" }}>
                <Settings fontSize="small" />
              </ListItemIcon>
              <ListItemText
                primary="Settings"
                primaryTypographyProps={{
                  fontSize: "0.875rem",
                  fontWeight: 500,
                }}
              />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton
              onClick={handleLogout}
              sx={{
                borderRadius: 1,
                py: 1,
                px: 2,
                color: "error.main",
                "&:hover": {
                  bgcolor: alpha(theme.palette.error.main, 0.08),
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: "inherit" }}>
                <Logout fontSize="small" />
              </ListItemIcon>
              <ListItemText
                primary="Logout"
                primaryTypographyProps={{
                  fontSize: "0.875rem",
                  fontWeight: 500,
                }}
              />
            </ListItemButton>
          </ListItem>
        </List>
      </Box>
    </Box>
  );

  return (
    <Box>
      {/* Desktop Sidebar - Always visible */}
      <Box
        sx={{
          width: 288,
          display: { xs: "none", lg: "block" },
          flexShrink: 0,
        }}
      >
        {drawerContent}
      </Box>

      {/* Mobile Sidebar - Drawer */}
      <Drawer
        variant="temporary"
        open={open}
        onClose={onClose}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          display: { xs: "block", lg: "none" },
          "& .MuiDrawer-paper": {
            boxSizing: "border-box",
            width: 288,
          },
        }}
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
}
