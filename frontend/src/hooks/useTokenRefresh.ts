import { useEffect, useRef, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from './redux';
import { updateTokens, clearAuth, selectToken, selectRefreshToken } from '@/store/slices/authSlice';
import { useRefreshTokenMutation } from '@/store/api/authApi';
import { TOKEN_REFRESH_THRESHOLD, ROUTES } from '@/constants';

interface UseTokenRefreshConfig {
  refreshThresholdMinutes?: number;
  maxRetries?: number;
  enabled?: boolean;
}

interface UseTokenRefreshReturn {
  isRefreshing: boolean;
  lastRefreshTime: Date | null;
  error: string | null;
}

/**
 * Hook for automatic token refresh management
 * Monitors JWT token expiration and automatically refreshes before expiry
 * 
 * @param config - Configuration options
 * @returns Token refresh state
 */
export const useTokenRefresh = (config: UseTokenRefreshConfig = {}): UseTokenRefreshReturn => {
  const {
    refreshThresholdMinutes = 5,
    maxRetries = 3,
    enabled = true,
  } = config;

  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const accessToken = useAppSelector(selectToken);
  const refreshToken = useAppSelector(selectRefreshToken);
  
  const [refreshTokenMutation] = useRefreshTokenMutation();
  
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastRefreshTime, setLastRefreshTime] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);
  
  const refreshTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const retryCountRef = useRef(0);
  const isRefreshingRef = useRef(false);

  /**
   * Decode JWT token and extract expiration time
   */
  const decodeToken = useCallback((token: string): { exp: number } | null => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload;
    } catch (error) {
      console.error('Failed to decode token:', error);
      return null;
    }
  }, []);

  /**
   * Calculate time until token expiration in milliseconds
   */
  const getTimeUntilExpiry = useCallback((token: string): number => {
    const decoded = decodeToken(token);
    if (!decoded || !decoded.exp) {
      return 0;
    }

    const expirationTime = decoded.exp * 1000; // Convert to milliseconds
    const currentTime = Date.now();
    return expirationTime - currentTime;
  }, [decodeToken]);

  /**
   * Check if token needs refresh (within threshold of expiry)
   */
  const shouldRefreshToken = useCallback((token: string): boolean => {
    const timeUntilExpiry = getTimeUntilExpiry(token);
    const thresholdMs = refreshThresholdMinutes * 60 * 1000;
    return timeUntilExpiry > 0 && timeUntilExpiry <= thresholdMs;
  }, [getTimeUntilExpiry, refreshThresholdMinutes]);

  /**
   * Perform token refresh with retry logic and exponential backoff
   */
  const performTokenRefresh = useCallback(async (): Promise<boolean> => {
    // Prevent concurrent refresh attempts
    if (isRefreshingRef.current) {
      return false;
    }

    if (!refreshToken) {
      console.warn('No refresh token available');
      dispatch(clearAuth());
      navigate(ROUTES.LOGIN);
      return false;
    }

    isRefreshingRef.current = true;
    setIsRefreshing(true);
    setError(null);

    let attempt = 0;
    let success = false;

    while (attempt < maxRetries && !success) {
      try {
        // Calculate exponential backoff delay
        if (attempt > 0) {
          const backoffDelay = Math.min(1000 * Math.pow(2, attempt - 1), 10000);
          await new Promise(resolve => setTimeout(resolve, backoffDelay));
        }

        const response = await refreshTokenMutation({ token: refreshToken }).unwrap();
        
        if (response && response.token) {
          // Update both access and refresh tokens in Redux store and localStorage
          dispatch(updateTokens({ 
            token: response.token, 
            refreshToken: response.refreshToken 
          }));
          setLastRefreshTime(new Date());
          retryCountRef.current = 0;
          success = true;
          
          console.log('Token refreshed successfully');
        } else {
          throw new Error('Invalid refresh response');
        }
      } catch (err: any) {
        attempt++;
        const errorMessage = err?.data?.message || err?.message || 'Token refresh failed';
        
        console.error(`Token refresh attempt ${attempt} failed:`, errorMessage);
        
        if (attempt >= maxRetries) {
          // Max retries reached, redirect to login
          setError(errorMessage);
          dispatch(clearAuth());
          navigate(ROUTES.LOGIN);
          break;
        }
      }
    }

    isRefreshingRef.current = false;
    setIsRefreshing(false);
    
    return success;
  }, [refreshToken, maxRetries, refreshTokenMutation, dispatch, navigate]);

  /**
   * Schedule next token refresh check
   */
  const scheduleTokenRefresh = useCallback(() => {
    // Clear existing timeout
    if (refreshTimeoutRef.current) {
      clearTimeout(refreshTimeoutRef.current);
      refreshTimeoutRef.current = null;
    }

    if (!accessToken || !enabled) {
      return;
    }

    const timeUntilExpiry = getTimeUntilExpiry(accessToken);
    
    if (timeUntilExpiry <= 0) {
      // Token already expired, try to refresh immediately
      performTokenRefresh();
      return;
    }

    const thresholdMs = refreshThresholdMinutes * 60 * 1000;
    
    if (timeUntilExpiry <= thresholdMs) {
      // Token is within refresh threshold, refresh now
      performTokenRefresh();
    } else {
      // Schedule refresh for when token reaches threshold
      const timeUntilRefresh = timeUntilExpiry - thresholdMs;
      
      refreshTimeoutRef.current = setTimeout(() => {
        performTokenRefresh();
      }, timeUntilRefresh);
      
      console.log(`Token refresh scheduled in ${Math.round(timeUntilRefresh / 1000)} seconds`);
    }
  }, [accessToken, enabled, getTimeUntilExpiry, refreshThresholdMinutes, performTokenRefresh]);

  /**
   * Initialize token refresh monitoring
   */
  useEffect(() => {
    if (!enabled || !accessToken) {
      return;
    }

    scheduleTokenRefresh();

    // Cleanup on unmount
    return () => {
      if (refreshTimeoutRef.current) {
        clearTimeout(refreshTimeoutRef.current);
        refreshTimeoutRef.current = null;
      }
    };
  }, [accessToken, enabled, scheduleTokenRefresh]);

  /**
   * Handle visibility change - refresh token when tab becomes visible
   */
  useEffect(() => {
    if (!enabled) {
      return;
    }

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible' && accessToken) {
        // Check if token needs refresh when user returns to tab
        if (shouldRefreshToken(accessToken)) {
          performTokenRefresh();
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [enabled, accessToken, shouldRefreshToken, performTokenRefresh]);

  return {
    isRefreshing,
    lastRefreshTime,
    error,
  };
};

export default useTokenRefresh;
