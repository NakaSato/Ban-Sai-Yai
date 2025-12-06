"use client";

import { useState } from "react";
import {
  AppBar,
  Toolbar,
  Button,
  Box,
  Typography,
  Menu,
  MenuItem,
  Chip,
  IconButton,
  styled,
  alpha,
  InputBase,
  Badge,
  useTheme,
  useMediaQuery,
} from "@mui/material";
import {
  ExpandMore,
  BarChart,
  Groups,
  Person,
  Settings,
  Home,
  WorkspacePremium,
  Search,
  Notifications,
  Menu as MenuIcon,
  LightMode,
  DarkMode,
} from "@mui/icons-material";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import { useAppSelector, useAppDispatch } from "@/hooks/redux";
import { setTheme } from "@/store/slices/uiSlice";

// Create a custom theme for better styling
const theme = createTheme({
  palette: {
    primary: {
      main: "#1976d2",
    },
    secondary: {
      main: "#dc004e",
    },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: "rgba(255, 255, 255, 0.95)",
          backdropFilter: "blur(12px)",
          borderBottom: "1px solid rgba(0, 0, 0, 0.08)",
        },
      },
    },
  },
});

interface NavbarProps {
  userRole: string;
  onRoleChange: (role: string) => void;
  onMobileSidebarToggle?: (open: boolean) => void;
  mobileSidebarOpen?: boolean;
}

const roles = [
  {
    id: "officer",
    label: "Officer",
    sublabel: "Teller Operations",
    icon: BarChart,
    color: "#1976d2",
    gradient: "linear-gradient(135deg, #2196F3 0%, #00BCD4 100%)",
  },
  {
    id: "secretary",
    label: "Secretary",
    sublabel: "Accounting Control",
    icon: Groups,
    color: "#9c27b0",
    gradient: "linear-gradient(135deg, #9C27B0 0%, #E91E63 100%)",
  },
  {
    id: "president",
    label: "President",
    sublabel: "Executive Strategy",
    icon: WorkspacePremium,
    color: "#ff9800",
    gradient: "linear-gradient(135deg, #FFC107 0%, #FF9800 100%)",
  },
  {
    id: "member",
    label: "Member",
    sublabel: "Personal Finance",
    icon: Person,
    color: "#4caf50",
    gradient: "linear-gradient(135deg, #4CAF50 0%, #009688 100%)",
  },
  {
    id: "admin",
    label: "Admin",
    sublabel: "System Management",
    icon: Settings,
    color: "#607d8b",
    gradient: "linear-gradient(135deg, #607D8B 0%, #9E9E9E 100%)",
  },
];

// Styled components for custom styling
const StyledLogoContainer = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  gap: theme.spacing(1),
  minWidth: 0,
}));

const StyledLogoIcon = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  width: 48,
  height: 48,
  borderRadius: 12,
  background: "linear-gradient(135deg, #1976d2 0%, #dc004e 100%)",
  boxShadow: theme.shadows[4],
}));

const StyledPeriodContainer = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  gap: theme.spacing(1),
  padding: theme.spacing(1, 2),
  borderRadius: 8,
  backgroundColor: alpha(theme.palette.background.paper, 0.6),
  border: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  [theme.breakpoints.down("sm")]: {
    display: "none",
  },
}));

const StyledStatusIndicator = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  gap: theme.spacing(0.5),
  paddingLeft: theme.spacing(1.5),
  borderLeft: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  [theme.breakpoints.down("md")]: {
    display: "none",
  },
}));

const StyledTabNav = styled(Box)(({ theme }) => ({
  borderBottom: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  backgroundColor: alpha(theme.palette.background.paper, 0.3),
  backdropFilter: "blur(4px)",
  position: "sticky",
  top: 73,
  zIndex: 40,
}));

const StyledRoleButton = styled(Button)<{
  isActive: boolean;
  gradient?: string;
}>(({ theme, isActive, gradient }) => ({
  display: "flex",
  alignItems: "center",
  gap: theme.spacing(0.5),
  padding: theme.spacing(0.75, 1.5),
  borderRadius: 8,
  fontWeight: 500,
  fontSize: "0.875rem",
  transition: "all 0.2s ease-in-out",
  minWidth: "auto",
  textTransform: "none",
  ...(isActive && gradient
    ? {
        background: gradient,
        color: "white",
        boxShadow: theme.shadows[4],
        "&:hover": {
          background: gradient,
          transform: "translateY(-1px)",
          boxShadow: theme.shadows[6],
        },
      }
    : {
        color: theme.palette.text.secondary,
        "&:hover": {
          color: theme.palette.text.primary,
          backgroundColor: alpha(theme.palette.action.hover, 0.5),
        },
      }),
}));

const StyledDropdownButton = styled(Button)(({ theme }) => ({
  gap: theme.spacing(0.5),
  height: 40,
  padding: theme.spacing(0, 1),
  borderRadius: 8,
  border: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  backgroundColor: alpha(theme.palette.background.paper, 0.8),
  "&:hover": {
    backgroundColor: alpha(theme.palette.background.paper, 0.9),
  },
}));

const StyledDropdownMenu = styled(Menu)(({ theme }) => ({
  "& .MuiPaper-root": {
    minWidth: 256,
    borderRadius: 12,
    boxShadow: theme.shadows[8],
    border: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
    overflow: "hidden",
  },
}));

const StyledMenuItem = styled(MenuItem)<{
  isActive: boolean;
  gradient?: string;
}>(({ theme, isActive, gradient }) => ({
  display: "flex",
  alignItems: "flex-start",
  gap: theme.spacing(1.5),
  padding: theme.spacing(1.25, 1.5),
  borderRadius: 8,
  margin: theme.spacing(0.25),
  transition: "all 0.2s ease-in-out",
  ...(isActive && gradient
    ? {
        background: gradient,
        color: "white",
        "&:hover": {
          background: gradient,
          opacity: 0.9,
        },
        "& .MuiTypography-caption": {
          color: "rgba(255, 255, 255, 0.8)",
        },
      }
    : {
        "&:hover": {
          backgroundColor: alpha(theme.palette.action.hover, 0.5),
        },
      }),
}));

const StyledSearchContainer = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  position: "relative",
  flex: 1,
  maxWidth: 400,
  [theme.breakpoints.down("sm")]: {
    display: "none",
  },
}));

const StyledSearchInput = styled(InputBase)(({ theme }) => ({
  width: "100%",
  paddingLeft: theme.spacing(4),
  paddingRight: theme.spacing(2),
  paddingY: theme.spacing(1),
  borderRadius: 8,
  backgroundColor: alpha(theme.palette.background.paper, 0.8),
  border: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  fontSize: "0.875rem",
  transition: "all 0.2s ease-in-out",
  "&:focus-within": {
    backgroundColor: theme.palette.background.paper,
    borderColor: theme.palette.primary.main,
    boxShadow: `0 0 0 2px ${alpha(theme.palette.primary.main, 0.2)}`,
  },
  "&::placeholder": {
    color: theme.palette.text.secondary,
  },
}));

const StyledMobileSearchContainer = styled(Box)(({ theme }) => ({
  display: "none",
  padding: theme.spacing(2, 2, 1),
  borderTop: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  [theme.breakpoints.down("sm")]: {
    display: "block",
  },
}));

const StyledActionButtonsContainer = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  gap: theme.spacing(1),
  [theme.breakpoints.down("sm")]: {
    gap: theme.spacing(0.5),
  },
}));

const StyledDateContainer = styled(Box)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  gap: theme.spacing(2),
  padding: theme.spacing(1, 2),
  borderRadius: 8,
  backgroundColor: alpha(theme.palette.background.paper, 0.6),
  border: `1px solid ${alpha(theme.palette.divider, 0.4)}`,
  [theme.breakpoints.down("md")]: {
    display: "none",
  },
}));

export default function Navbar({
  userRole,
  onRoleChange,
  onMobileSidebarToggle,
  mobileSidebarOpen = false,
}: NavbarProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [mobileSearchOpen, setMobileSearchOpen] = useState(false);
  const currentRole = roles.find((r) => r.id === userRole);
  const dispatch = useAppDispatch();
  const appTheme = useAppSelector((state) => state.ui.theme);
  const muiTheme = useTheme();
  const isMobile = useMediaQuery(muiTheme.breakpoints.down("sm"));

  const toggleTheme = () => {
    dispatch(setTheme(appTheme === "dark" ? "light" : "dark"));
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleRoleChange = (role: string) => {
    onRoleChange(role);
    handleMenuClose();
  };

  const handleMobileSidebarToggle = () => {
    if (onMobileSidebarToggle) {
      onMobileSidebarToggle(!mobileSidebarOpen);
    }
  };

  const currentDate = new Date().toLocaleDateString("th-TH", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  });

  return (
    <ThemeProvider theme={theme}>
      {/* Main Header */}
      <AppBar position="sticky" elevation={0}>
        <Toolbar sx={{ maxWidth: 1200, mx: "auto", px: 2, py: 1 }}>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              width: 1,
              gap: 2,
            }}
          >
            {/* Left: Mobile Menu + Logo (Mobile) */}
            {isMobile && (
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  gap: 1,
                  minWidth: 0,
                }}
              >
                <IconButton
                  size="small"
                  onClick={handleMobileSidebarToggle}
                  sx={{ p: 0.5 }}
                >
                  <MenuIcon sx={{ fontSize: 20 }} />
                </IconButton>
                <Typography
                  variant="h6"
                  noWrap
                  sx={{ fontSize: "1rem", fontWeight: "bold" }}
                >
                  Ban Sai Yai
                </Typography>
              </Box>
            )}

            {/* Desktop Logo */}
            {!isMobile && (
              <StyledLogoContainer>
                <StyledLogoIcon>
                  <Home sx={{ fontSize: 24, color: "white" }} />
                </StyledLogoIcon>
                <Box sx={{ minWidth: 0 }}>
                  <Typography
                    variant="h6"
                    component="h1"
                    sx={{
                      fontSize: "1.25rem",
                      fontWeight: "bold",
                      lineHeight: 1.2,
                    }}
                  >
                    Ban Sai Yai
                  </Typography>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ fontSize: "0.75rem" }}
                  >
                    Savings & Loan
                  </Typography>
                </Box>
              </StyledLogoContainer>
            )}

            {/* Center: Search Bar (Desktop) */}
            {!isMobile && (
              <StyledSearchContainer>
                <Search
                  sx={{
                    position: "absolute",
                    left: 12,
                    top: "50%",
                    transform: "translateY(-50%)",
                    fontSize: 16,
                    color: "text.secondary",
                  }}
                />
                <StyledSearchInput placeholder="Search member..." fullWidth />
              </StyledSearchContainer>
            )}

            {/* Right: Actions */}
            <StyledActionButtonsContainer>
              {/* Theme Toggle */}
              <IconButton
                size="small"
                onClick={toggleTheme}
                title={`Switch to ${
                  appTheme === "dark" ? "light" : "dark"
                } mode`}
                sx={{ p: 0.5 }}
              >
                {appTheme === "dark" ? (
                  <LightMode sx={{ fontSize: 18, color: "warning.main" }} />
                ) : (
                  <DarkMode sx={{ fontSize: 18, color: "text.secondary" }} />
                )}
              </IconButton>

              {/* Notifications */}
              <Badge badgeContent={3} color="error">
                <IconButton size="small" sx={{ p: 0.5 }}>
                  <Notifications
                    sx={{ fontSize: 18, color: "text.secondary" }}
                  />
                </IconButton>
              </Badge>

              {/* Date & Period Status (Desktop) */}
              {!isMobile && (
                <StyledDateContainer>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Typography variant="caption" color="text.secondary">
                      Period:
                    </Typography>
                    <Typography variant="body2" fontWeight="600">
                      August 2024
                    </Typography>
                  </Box>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Box
                      sx={{
                        width: 6,
                        height: 6,
                        borderRadius: "50%",
                        backgroundColor: "#4caf50",
                        animation: "pulse 2s infinite",
                        "@keyframes pulse": {
                          "0%": { opacity: 1 },
                          "50%": { opacity: 0.5 },
                          "100%": { opacity: 1 },
                        },
                      }}
                    />
                    <Typography
                      variant="caption"
                      sx={{ color: "#4caf50", fontWeight: "medium" }}
                    >
                      OPEN
                    </Typography>
                  </Box>
                </StyledDateContainer>
              )}

              {/* Date Display (Large Desktop) */}
              {!isMobile && (
                <Box sx={{ display: { xs: "none", lg: "block" } } as any}>
                  <Typography variant="caption" color="text.secondary">
                    {currentDate}
                  </Typography>
                </Box>
              )}

              {/* Role Selector */}
              <Box>
                <StyledDropdownButton
                  onClick={handleMenuOpen}
                  endIcon={
                    <ExpandMore
                      sx={{
                        transition: "transform 0.2s",
                        transform: anchorEl ? "rotate(180deg)" : "none",
                      }}
                    />
                  }
                >
                  {currentRole && (
                    <>
                      <currentRole.icon sx={{ fontSize: 16 }} />
                      <Typography
                        variant="body2"
                        sx={{ display: { xs: "none", sm: "block" } } as any}
                      >
                        {currentRole.label}
                      </Typography>
                      <Typography
                        variant="body2"
                        sx={{ display: { xs: "block", sm: "none" } } as any}
                      >
                        {currentRole.label.slice(0, 3)}
                      </Typography>
                    </>
                  )}
                </StyledDropdownButton>

                {/* Dropdown Menu */}
                <StyledDropdownMenu
                  anchorEl={anchorEl}
                  open={Boolean(anchorEl)}
                  onClose={handleMenuClose}
                  anchorOrigin={{
                    vertical: "bottom",
                    horizontal: "right",
                  }}
                  transformOrigin={{
                    vertical: "top",
                    horizontal: "right",
                  }}
                >
                  {roles.map((role) => {
                    const Icon = role.icon;
                    const isActive = userRole === role.id;
                    return (
                      <StyledMenuItem
                        key={role.id}
                        onClick={() => handleRoleChange(role.id)}
                        isActive={isActive}
                        gradient={role.gradient}
                      >
                        <Icon sx={{ fontSize: 20, mt: 0.5, flexShrink: 0 }} />
                        <Box sx={{ textAlign: "left" }}>
                          <Typography
                            variant="body2"
                            fontWeight="600"
                            sx={{ fontSize: "0.875rem" }}
                          >
                            {role.label}
                          </Typography>
                          <Typography
                            variant="caption"
                            sx={{ fontSize: "0.75rem" }}
                          >
                            {role.sublabel}
                          </Typography>
                        </Box>
                      </StyledMenuItem>
                    );
                  })}
                </StyledDropdownMenu>
              </Box>
            </StyledActionButtonsContainer>
          </Box>
        </Toolbar>

        {/* Mobile Search Bar */}
        <StyledMobileSearchContainer>
          <StyledSearchContainer sx={{ maxWidth: "none" }}>
            <Search
              sx={{
                position: "absolute",
                left: 12,
                top: "50%",
                transform: "translateY(-50%)",
                fontSize: 16,
                color: "text.secondary",
              }}
            />
            <StyledSearchInput placeholder="Search member..." fullWidth />
          </StyledSearchContainer>
        </StyledMobileSearchContainer>
      </AppBar>

      {/* Tab Navigation - Horizontal Role Buttons */}
      <StyledTabNav>
        <Box sx={{ maxWidth: 1200, mx: "auto", px: 2 }}>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 0.5,
              overflowX: "auto",
              py: 1,
            }}
          >
            {roles.map((role) => {
              const Icon = role.icon;
              const isActive = userRole === role.id;
              return (
                <StyledRoleButton
                  key={role.id}
                  onClick={() => onRoleChange(role.id)}
                  isActive={isActive}
                  gradient={role.gradient}
                >
                  <Icon sx={{ fontSize: 16 }} />
                  <Typography
                    variant="body2"
                    sx={{ display: { xs: "none", sm: "block" } } as any}
                  >
                    {role.label}
                  </Typography>
                  <Typography
                    variant="caption"
                    sx={
                      {
                        display: { xs: "block", sm: "none" },
                        fontSize: "0.75rem",
                      } as any
                    }
                  >
                    {role.label.slice(0, 3)}
                  </Typography>
                </StyledRoleButton>
              );
            })}
          </Box>
        </Box>
      </StyledTabNav>
    </ThemeProvider>
  );
}
