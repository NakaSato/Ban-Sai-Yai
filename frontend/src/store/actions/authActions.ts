import { AppDispatch } from '@/store';
import { clearAuth, setAuth, setError, setLoading } from '@/store/slices/authSlice';
import { authApi } from '@/store/api/authApi';
import { LoginRequest, LoginResponse } from '@/types';

/**
 * Enhanced login action that handles rememberMe and refresh tokens
 */
export const loginUser = (credentials: LoginRequest) => async (dispatch: AppDispatch) => {
  try {
    dispatch(setLoading(true));
    
    // Call the login mutation
    const response = await dispatch(
      authApi.endpoints.login.initiate(credentials)
    ).unwrap();
    
    // Set auth state with user, token, and optional refresh token
    dispatch(setAuth({
      user: response.user,
      token: response.token,
      refreshToken: response.refreshToken,
    }));
    
    return response;
  } catch (error: any) {
    const errorMessage = error?.data?.message || error?.message || 'Login failed';
    dispatch(setError(errorMessage));
    throw error;
  }
};

/**
 * Enhanced logout action that calls backend API before clearing tokens
 */
export const logoutUser = () => async (dispatch: AppDispatch) => {
  try {
    // Call the backend logout endpoint to invalidate tokens
    await dispatch(
      authApi.endpoints.logout.initiate()
    ).unwrap();
  } catch (error) {
    // Log the error but still clear local state
    console.error('Logout API call failed:', error);
  } finally {
    // Always clear auth state regardless of API call success
    dispatch(clearAuth());
  }
};

/**
 * Refresh token action that updates both access and refresh tokens
 */
export const refreshAuthToken = (refreshToken: string) => async (dispatch: AppDispatch) => {
  try {
    const response = await dispatch(
      authApi.endpoints.refreshToken.initiate({ token: refreshToken })
    ).unwrap();
    
    return response;
  } catch (error) {
    // If refresh fails, clear auth and throw error
    dispatch(clearAuth());
    throw error;
  }
};
