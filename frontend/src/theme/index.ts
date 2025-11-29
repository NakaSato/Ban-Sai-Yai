import { createTheme, ThemeOptions } from '@mui/material/styles';
import { THEME_COLORS } from '@/constants';

// Light theme configuration
const lightThemeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: THEME_COLORS.PRIMARY.MAIN,
      light: THEME_COLORS.PRIMARY.LIGHT,
      dark: THEME_COLORS.PRIMARY.DARK,
      contrastText: '#ffffff',
    },
    secondary: {
      main: THEME_COLORS.SECONDARY.MAIN,
      light: THEME_COLORS.SECONDARY.LIGHT,
      dark: THEME_COLORS.SECONDARY.DARK,
      contrastText: '#ffffff',
    },
    success: {
      main: THEME_COLORS.SUCCESS.MAIN,
      light: THEME_COLORS.SUCCESS.LIGHT,
      dark: THEME_COLORS.SUCCESS.DARK,
    },
    warning: {
      main: THEME_COLORS.WARNING.MAIN,
      light: THEME_COLORS.WARNING.LIGHT,
      dark: THEME_COLORS.WARNING.DARK,
    },
    error: {
      main: THEME_COLORS.ERROR.MAIN,
      light: THEME_COLORS.ERROR.LIGHT,
      dark: THEME_COLORS.ERROR.DARK,
    },
    info: {
      main: THEME_COLORS.INFO.MAIN,
      light: THEME_COLORS.INFO.LIGHT,
      dark: THEME_COLORS.INFO.DARK,
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
    text: {
      primary: '#212121',
      secondary: '#757575',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 600,
      lineHeight: 1.2,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 600,
      lineHeight: 1.3,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
      lineHeight: 1.5,
    },
    h6: {
      fontSize: '1rem',
      fontWeight: 600,
      lineHeight: 1.6,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.5,
    },
    body2: {
      fontSize: '0.875rem',
      lineHeight: 1.43,
    },
    subtitle1: {
      fontSize: '1rem',
      fontWeight: 500,
      lineHeight: 1.75,
    },
    subtitle2: {
      fontSize: '0.875rem',
      fontWeight: 500,
      lineHeight: 1.57,
    },
    button: {
      fontSize: '0.875rem',
      fontWeight: 500,
      textTransform: 'none' as const,
    },
    caption: {
      fontSize: '0.75rem',
      lineHeight: 1.66,
    },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
          fontWeight: 500,
          padding: '8px 16px',
        },
        contained: {
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
          '&:hover': {
            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.15)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
          borderRadius: 12,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
        elevation1: {
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        },
        elevation2: {
          boxShadow: '0 4px 8px rgba(0, 0, 0, 0.12)',
        },
        elevation3: {
          boxShadow: '0 6px 12px rgba(0, 0, 0, 0.15)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: '1px solid rgba(0, 0, 0, 0.12)',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          margin: '2px 8px',
          '&.Mui-selected': {
            backgroundColor: THEME_COLORS.PRIMARY.LIGHT,
            '&:hover': {
              backgroundColor: THEME_COLORS.PRIMARY.MAIN,
              color: 'white',
            },
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          backgroundColor: '#f5f5f5',
          fontWeight: 600,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          fontWeight: 500,
        },
      },
    },
  },
};

// Dark theme configuration
const darkThemeOptions: ThemeOptions = {
  ...lightThemeOptions,
  palette: {
    mode: 'dark',
    primary: {
      main: THEME_COLORS.PRIMARY.LIGHT,
      light: THEME_COLORS.PRIMARY.MAIN,
      dark: THEME_COLORS.PRIMARY.DARK,
      contrastText: '#ffffff',
    },
    secondary: {
      main: THEME_COLORS.SECONDARY.LIGHT,
      light: THEME_COLORS.SECONDARY.MAIN,
      dark: THEME_COLORS.SECONDARY.DARK,
      contrastText: '#000000',
    },
    background: {
      default: '#121212',
      paper: '#1e1e1e',
    },
    text: {
      primary: '#ffffff',
      secondary: '#b0b0b0',
    },
  },
};

export const lightTheme = createTheme(lightThemeOptions);
export const darkTheme = createTheme(darkThemeOptions);

export const getTheme = (mode: 'light' | 'dark') => {
  return mode === 'dark' ? darkTheme : lightTheme;
};

export default lightTheme;
