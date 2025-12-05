import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  Container,
  Paper,
  Box,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  IconButton,
  InputAdornment,
  Checkbox,
  FormControlLabel,
  Divider,
  Grid,
} from "@mui/material";
import {
  Visibility,
  VisibilityOff,
  Email,
  Lock,
  AccountBalance,
} from "@mui/icons-material";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import {
  setAuth,
  setLoading,
  setError,
  clearError,
} from "@/store/slices/authSlice";
import { useLoginMutation } from "@/store/api/authApi";
import { useLoginValidation } from "@/hooks/useLoginValidation";

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [loginMutation] = useLoginMutation();
  const { isLoading, error } = useAppSelector((state) => state.auth);

  const [formData, setFormData] = useState({
    username: "",
    password: "",
    rememberMe: false,
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [lockoutTime, setLockoutTime] = useState<number | null>(null);

  const [touched, setTouched] = useState({
    username: false,
    password: false,
  });

  // Use validation hook
  const { touchedErrors, isValid } = useLoginValidation(
    formData.username,
    formData.password,
    touched
  );

  // Handle countdown timer for lockout
  useEffect(() => {
    if (lockoutTime && lockoutTime > 0) {
      const timer = setInterval(() => {
        setLockoutTime((prev) => {
          if (prev === null || prev <= 1) {
            return null;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [lockoutTime]);

  // Parse lockout time from error message
  useEffect(() => {
    if (error && error.includes("locked")) {
      // Try to extract time from error message (e.g., "Account temporarily locked. Try again in 10 minutes")
      const match = error.match(/(\d+)\s*minute/i);
      if (match) {
        setLockoutTime(parseInt(match[1]) * 60);
      }
    } else {
      setLockoutTime(null);
    }
  }, [error]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, checked } = e.target;

    // Clear errors when user starts typing
    if (error) {
      dispatch(clearError());
    }

    if (name === "rememberMe") {
      setFormData({
        ...formData,
        rememberMe: checked,
      });
    } else {
      setFormData({
        ...formData,
        [name]: value,
      });
    }
  };

  const handleBlur = (field: "username" | "password") => {
    setTouched({
      ...touched,
      [field]: true,
    });
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Mark all fields as touched
    setTouched({
      username: true,
      password: true,
    });

    // Don't submit if validation fails
    if (!isValid) {
      return;
    }

    try {
      dispatch(setLoading(true));
      const response = await loginMutation({
        username: formData.username,
        password: formData.password,
        rememberMe: formData.rememberMe,
      }).unwrap();

      // Set auth with refresh token if provided
      dispatch(
        setAuth({
          user: response.user,
          token: response.token,
          refreshToken: response.refreshToken,
        })
      );

      // Show success message briefly
      setShowSuccess(true);
      setTimeout(() => {
        navigate("/dashboard");
      }, 1000);
    } catch (error: any) {
      const errorMessage =
        error?.data?.message || error?.message || "Login failed";
      dispatch(setError(errorMessage));
    }
  };

  // Handle Enter key submission from any field
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !isLoading && isValid) {
      handleSubmit(e as any);
    } else if (e.key === "Escape" && error) {
      dispatch(clearError());
    }
  };

  // Format lockout time for display
  const formatLockoutTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    if (minutes > 0) {
      return `${minutes} minute${minutes !== 1 ? "s" : ""} ${secs} second${
        secs !== 1 ? "s" : ""
      }`;
    }
    return `${secs} second${secs !== 1 ? "s" : ""}`;
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        p: 2,
      }}
    >
      <Container maxWidth="lg">
        <Paper
          elevation={24}
          sx={{
            borderRadius: 4,
            overflow: "hidden",
            maxWidth: 1000,
            mx: "auto",
          }}
        >
          <Grid container>
            {/* Left Side - Branding */}
            <Grid
              item
              xs={12}
              md={6}
              sx={{
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                color: "white",
                p: { xs: 4, md: 6 },
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                textAlign: "center",
              }}
            >
              <Typography variant="h3" fontWeight="bold" gutterBottom>
                Bansaiyai
              </Typography>
              <Typography variant="h6" sx={{ opacity: 0.9, mb: 2 }}>
                Village Savings & Loan Association
              </Typography>
              <Typography variant="body1" sx={{ opacity: 0.85, maxWidth: 400 }}>
                Access your financial management system to track savings, loans,
                and member transactions.
              </Typography>
            </Grid>

            {/* Right Side - Login Form */}
            <Grid
              item
              xs={12}
              md={6}
              sx={{
                p: { xs: 3, sm: 4, md: 6 },
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
              }}
            >
              <Box sx={{ mb: 4, textAlign: "center" }}>
                <Typography
                  component="h1"
                  variant="h4"
                  fontWeight="bold"
                  color="primary"
                  gutterBottom
                >
                  Welcome Back
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Sign in to access your dashboard
                </Typography>
              </Box>

              {showSuccess && (
                <Alert
                  severity="success"
                  sx={{ width: "100%", mb: 2, borderRadius: 2 }}
                  role="alert"
                  aria-live="polite"
                  aria-atomic="true"
                >
                  Login successful! Redirecting...
                </Alert>
              )}

              {error && (
                <Alert
                  severity="error"
                  sx={{ width: "100%", mb: 2, borderRadius: 2 }}
                  role="alert"
                  aria-live="assertive"
                  aria-atomic="true"
                >
                  {lockoutTime && lockoutTime > 0
                    ? `Account temporarily locked. Try again in ${formatLockoutTime(
                        lockoutTime
                      )}`
                    : error}
                </Alert>
              )}

              {/* Screen reader status announcements */}
              <Box
                role="status"
                aria-live="polite"
                aria-atomic="true"
                sx={{
                  position: "absolute",
                  left: "-10000px",
                  width: "1px",
                  height: "1px",
                  overflow: "hidden",
                }}
              >
                {isLoading && "Signing in, please wait"}
              </Box>

              <Box
                component="form"
                onSubmit={handleSubmit}
                onKeyDown={handleKeyDown}
                sx={{ width: "100%" }}
                aria-label="Login form"
              >
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="username"
                  label="Username"
                  name="username"
                  autoComplete="username"
                  autoFocus
                  placeholder="admin"
                  value={formData.username}
                  onChange={handleChange}
                  onBlur={() => handleBlur("username")}
                  disabled={isLoading}
                  error={!!touchedErrors.username}
                  helperText={touchedErrors.username || " "}
                  inputProps={{
                    "aria-label": "Username",
                    "aria-describedby": touchedErrors.username
                      ? "username-helper-text"
                      : undefined,
                    "aria-invalid": !!touchedErrors.username,
                    "aria-required": "true",
                  }}
                  FormHelperTextProps={{
                    id: "username-helper-text",
                    role: touchedErrors.username ? "alert" : undefined,
                  }}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Email color="action" />
                      </InputAdornment>
                    ),
                  }}
                  sx={{
                    "& .MuiOutlinedInput-root": {
                      borderRadius: 2,
                      transition: "all 0.3s",
                      "&:hover": {
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                      },
                      "&.Mui-focused": {
                        boxShadow: "0 4px 12px rgba(102, 126, 234, 0.2)",
                      },
                    },
                  }}
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  name="password"
                  label="Password"
                  type={showPassword ? "text" : "password"}
                  id="password"
                  autoComplete="current-password"
                  placeholder="admin123"
                  value={formData.password}
                  onChange={handleChange}
                  onBlur={() => handleBlur("password")}
                  disabled={isLoading}
                  error={!!touchedErrors.password}
                  helperText={touchedErrors.password || " "}
                  inputProps={{
                    "aria-label": "Password",
                    "aria-describedby": touchedErrors.password
                      ? "password-helper-text"
                      : undefined,
                    "aria-invalid": !!touchedErrors.password,
                    "aria-required": "true",
                  }}
                  FormHelperTextProps={{
                    id: "password-helper-text",
                    role: touchedErrors.password ? "alert" : undefined,
                  }}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Lock color="action" />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label={
                            showPassword ? "Hide password" : "Show password"
                          }
                          onClick={togglePasswordVisibility}
                          onMouseDown={(e) => e.preventDefault()}
                          edge="end"
                          disabled={isLoading}
                          tabIndex={-1}
                          sx={{
                            minWidth: 44,
                            minHeight: 44,
                          }}
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                  sx={{
                    "& .MuiOutlinedInput-root": {
                      borderRadius: 2,
                      transition: "all 0.3s",
                      "&:hover": {
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                      },
                      "&.Mui-focused": {
                        boxShadow: "0 4px 12px rgba(102, 126, 234, 0.2)",
                      },
                    },
                  }}
                />

                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mt: 1,
                    mb: 2,
                  }}
                >
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formData.rememberMe}
                        onChange={handleChange}
                        name="rememberMe"
                        color="primary"
                        disabled={isLoading}
                        inputProps={{
                          "aria-label": "Remember me for future sessions",
                        }}
                      />
                    }
                    label="Remember Me"
                  />
                  <Link
                    to="/forgot-password"
                    style={{
                      color: "#667eea",
                      textDecoration: "none",
                      fontSize: "0.875rem",
                      fontWeight: 500,
                    }}
                  >
                    Forgot Password?
                  </Link>
                </Box>

                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{
                    mt: 2,
                    mb: 2,
                    minHeight: 48,
                    py: 1.5,
                    borderRadius: 2,
                    background:
                      "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                    fontWeight: "bold",
                    fontSize: "1rem",
                    textTransform: "none",
                    boxShadow: "0 4px 12px rgba(102, 126, 234, 0.4)",
                    transition: "all 0.3s",
                    "&:hover": {
                      background:
                        "linear-gradient(135deg, #764ba2 0%, #667eea 100%)",
                      boxShadow: "0 6px 16px rgba(102, 126, 234, 0.5)",
                      transform: "translateY(-2px)",
                    },
                    "&:disabled": {
                      background: "linear-gradient(135deg, #ccc 0%, #999 100%)",
                      boxShadow: "none",
                    },
                  }}
                  disabled={isLoading || !isValid}
                  aria-label="Sign in to your account"
                  aria-busy={isLoading}
                  aria-disabled={isLoading || !isValid}
                >
                  {isLoading ? (
                    <>
                      <CircularProgress
                        size={24}
                        color="inherit"
                        aria-label="Signing in"
                      />
                      <span style={{ marginLeft: 8 }}>Signing in...</span>
                    </>
                  ) : (
                    "Access Dashboard"
                  )}
                </Button>

                <Divider sx={{ my: 3 }}>
                  <Typography variant="body2" color="text.secondary">
                    System Access
                  </Typography>
                </Divider>

                <Typography
                  variant="body2"
                  color="text.secondary"
                  align="center"
                >
                  Need system access?{" "}
                  <Link
                    to="/request-access"
                    style={{
                      color: "#667eea",
                      textDecoration: "none",
                      fontWeight: 600,
                    }}
                  >
                    Request Access
                  </Link>
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>
      </Container>
    </Box>
  );
};

export default LoginPage;
