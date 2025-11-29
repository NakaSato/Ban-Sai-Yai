import { store } from '@/store';
import { updateToken, clearAuth } from '@/store/slices/authSlice';
import { AUTH_TOKEN_KEY, TOKEN_REFRESH_THRESHOLD } from '@/constants';

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

// Process the failed queue
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
  
  failedQueue = [];
};

// Check if token is expired or about to expire
const isTokenExpired = (token: string): boolean => {
  try {
    if (!token) return true;
    
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentTime = Date.now() / 1000;
    const expirationTime = payload.exp;
    
    // Check if token is expired or will expire within threshold
    return expirationTime - currentTime < TOKEN_REFRESH_THRESHOLD / 1000;
  } catch (error) {
    return true;
  }
};

// Refresh token function
export const refreshToken = async (): Promise<string> => {
  if (isRefreshing) {
    // If already refreshing, wait for it to complete
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject });
    });
  }

  const state = store.getState();
  const currentToken = state.auth.token;

  if (!currentToken) {
    processQueue(new Error('No token available'));
    return Promise.reject(new Error('No token available'));
  }

  // Check if token needs refresh
  if (!isTokenExpired(currentToken)) {
    processQueue(null, currentToken);
    return Promise.resolve(currentToken);
  }

  isRefreshing = true;

  try {
    const response = await fetch(`/api/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentToken}`,
      },
      body: JSON.stringify({ token: currentToken }),
    });

    if (!response.ok) {
      throw new Error('Token refresh failed');
    }

    const data = await response.json();
    const newToken = data.token;

    // Update token in store and localStorage
    store.dispatch(updateToken(newToken));
    localStorage.setItem(AUTH_TOKEN_KEY, newToken);

    processQueue(null, newToken);
    return newToken;
  } catch (error) {
    // If refresh fails, clear auth and redirect to login
    store.dispatch(clearAuth());
    localStorage.removeItem(AUTH_TOKEN_KEY);
    
    processQueue(error);
    
    // Redirect to login page
    window.location.href = '/login';
    
    return Promise.reject(error);
  } finally {
    isRefreshing = false;
  }
};

// Enhanced fetch wrapper with automatic token refresh
export const fetchWithRefresh = async (url: string, options: RequestInit = {}): Promise<Response> => {
  const state = store.getState();
  let token = state.auth.token;

  // If no token, proceed without it
  if (!token) {
    return fetch(url, options);
  }

  // Check if token needs refresh
  if (isTokenExpired(token)) {
    try {
      token = await refreshToken();
    } catch (error) {
      // Refresh failed, the refreshToken function will handle redirect
      return Promise.reject(error);
    }
  }

  // Add authorization header
  const headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`,
  };

  return fetch(url, {
    ...options,
    headers,
  }).then(async (response) => {
    // If we get a 401, try to refresh and retry once
    if (response.status === 401 && !isRefreshing) {
      try {
        token = await refreshToken();
        
        // Retry the original request with new token
        return fetch(url, {
          ...options,
          headers: {
            ...options.headers,
            'Authorization': `Bearer ${token}`,
          },
        });
      } catch (error) {
        // Refresh failed, will be handled by refreshToken function
        return Promise.reject(error);
      }
    }
    
    return response;
  });
};

// Utility to get current token with refresh check
export const getCurrentToken = async (): Promise<string | null> => {
  const state = store.getState();
  const token = state.auth.token;

  if (!token) return null;

  if (isTokenExpired(token)) {
    try {
      return await refreshToken();
    } catch (error) {
      return null;
    }
  }

  return token;
};

// Setup token refresh interval
export const setupTokenRefresh = () => {
  // Check token every minute
  setInterval(async () => {
    const state = store.getState();
    const token = state.auth.token;

    if (token && isTokenExpired(token) && !isRefreshing) {
      try {
        await refreshToken();
      } catch (error) {
        // Error handled in refreshToken function
        console.warn('Token refresh failed:', error);
      }
    }
  }, 60000); // 1 minute
};

export default refreshToken;
