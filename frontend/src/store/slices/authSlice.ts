import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { User, AuthState } from '@/types';
import { AUTH_TOKEN_KEY, USER_KEY } from '@/constants';

const initialState: AuthState = {
  user: null,
  token: localStorage.getItem(AUTH_TOKEN_KEY),
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // Set loading state
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },

    // Set authentication state after successful login
    setAuth: (state, action: PayloadAction<{ user: User; token: string }>) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.isAuthenticated = true;
      state.isLoading = false;
      state.error = null;
      
      // Store in localStorage
      localStorage.setItem(AUTH_TOKEN_KEY, action.payload.token);
      localStorage.setItem(USER_KEY, JSON.stringify(action.payload.user));
    },

    // Clear authentication state after logout
    clearAuth: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.isLoading = false;
      state.error = null;
      
      // Remove from localStorage
      localStorage.removeItem(AUTH_TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },

    // Set error state
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
      state.isLoading = false;
    },

    // Clear error state
    clearError: (state) => {
      state.error = null;
    },

    // Update user information
    updateUser: (state, action: PayloadAction<Partial<User>>) => {
      if (state.user) {
        state.user = { ...state.user, ...action.payload };
        localStorage.setItem(USER_KEY, JSON.stringify(state.user));
      }
    },

    // Update token (for token refresh)
    updateToken: (state, action: PayloadAction<string>) => {
      state.token = action.payload;
      localStorage.setItem(AUTH_TOKEN_KEY, action.payload);
    },

    // Load user from storage
    loadUserFromStorage: (state) => {
      try {
        const userStr = localStorage.getItem(USER_KEY);
        const token = localStorage.getItem(AUTH_TOKEN_KEY);
        
        if (userStr && token) {
          const user = JSON.parse(userStr) as User;
          state.user = user;
          state.token = token;
          state.isAuthenticated = true;
          state.error = null;
        }
      } catch (error) {
        // Clear invalid stored data
        localStorage.removeItem(AUTH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      }
    },
  },
});

export const {
  setLoading,
  setAuth,
  clearAuth,
  setError,
  clearError,
  updateUser,
  updateToken,
  loadUserFromStorage,
} = authSlice.actions;

export default authSlice.reducer;

// Selectors
export const selectAuth = (state: { auth: AuthState }) => state.auth;
export const selectUser = (state: { auth: AuthState }) => state.auth.user;
export const selectToken = (state: { auth: AuthState }) => state.auth.token;
export const selectIsAuthenticated = (state: { auth: AuthState }) => state.auth.isAuthenticated;
export const selectIsLoading = (state: { auth: AuthState }) => state.auth.isLoading;
export const selectError = (state: { auth: AuthState }) => state.auth.error;
