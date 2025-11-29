import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UIState {
  theme: 'light' | 'dark';
  sidebarCollapsed: boolean;
  loading: boolean;
  notification: {
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'warning' | 'info';
  } | null;
  pageTitle: string;
  breadcrumbs: Array<{
    label: string;
    path?: string;
  }>;
}

const initialState: UIState = {
  theme: (localStorage.getItem('theme') as 'light' | 'dark') || 'light',
  sidebarCollapsed: localStorage.getItem('sidebar_collapsed') === 'true',
  loading: false,
  notification: null,
  pageTitle: 'Dashboard',
  breadcrumbs: [],
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    setTheme: (state, action: PayloadAction<'light' | 'dark'>) => {
      state.theme = action.payload;
      localStorage.setItem('theme', action.payload);
    },
    toggleSidebar: (state) => {
      state.sidebarCollapsed = !state.sidebarCollapsed;
      localStorage.setItem('sidebar_collapsed', state.sidebarCollapsed.toString());
    },
    setSidebarCollapsed: (state, action: PayloadAction<boolean>) => {
      state.sidebarCollapsed = action.payload;
      localStorage.setItem('sidebar_collapsed', action.payload.toString());
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    showNotification: (state, action: PayloadAction<{
      message: string;
      severity: 'success' | 'error' | 'warning' | 'info';
    }>) => {
      state.notification = {
        open: true,
        message: action.payload.message,
        severity: action.payload.severity,
      };
    },
    hideNotification: (state) => {
      if (state.notification) {
        state.notification.open = false;
      }
    },
    clearNotification: (state) => {
      state.notification = null;
    },
    setPageTitle: (state, action: PayloadAction<string>) => {
      state.pageTitle = action.payload;
      document.title = `${action.payload} - Bansaiyai Financial System`;
    },
    setBreadcrumbs: (state, action: PayloadAction<Array<{
      label: string;
      path?: string;
    }>>) => {
      state.breadcrumbs = action.payload;
    },
  },
});

export const {
  setTheme,
  toggleSidebar,
  setSidebarCollapsed,
  setLoading,
  showNotification,
  hideNotification,
  clearNotification,
  setPageTitle,
  setBreadcrumbs,
} = uiSlice.actions;

export default uiSlice.reducer;
