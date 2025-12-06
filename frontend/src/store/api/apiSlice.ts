import {
  createApi,
  fetchBaseQuery,
  retry,
  BaseQueryFn,
  FetchArgs,
  FetchBaseQueryError,
} from "@reduxjs/toolkit/query/react";
import { RootState } from "../index";
import { API_BASE_URL, REFRESH_TOKEN_KEY, AUTH_TOKEN_KEY } from "@/constants";
import { Mutex } from "async-mutex";

// Mutex to prevent multiple simultaneous token refresh attempts
const mutex = new Mutex();

// Base query with authorization
const baseQuery = fetchBaseQuery({
  baseUrl: API_BASE_URL,
  prepareHeaders: (headers, { getState }) => {
    const token = (getState() as RootState).auth.token;
    if (token) {
      headers.set("authorization", `Bearer ${token}`);
    }
    headers.set("Content-Type", "application/json");
    return headers;
  },
  // Add timeout
  timeout: 30000,
});

// Stagger retry with exponential backoff
const staggeredBaseQuery = retry(baseQuery, {
  maxRetries: 3,
  backoff: (attempt, maxRetries) => {
    // Exponential backoff: 1s, 2s, 4s
    const delay = Math.min(1000 * Math.pow(2, attempt), 8000);
    return new Promise((resolve) => setTimeout(resolve, delay));
  },
});

// Base query with re-authentication on 401
const baseQueryWithReauth: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  // Wait if another request is already refreshing the token
  await mutex.waitForUnlock();

  let result = await staggeredBaseQuery(args, api, extraOptions);

  // Handle 401 Unauthorized - try to refresh token
  if (result.error && result.error.status === 401) {
    // Check if we have a refresh token
    const refreshToken =
      (api.getState() as RootState).auth.refreshToken ||
      localStorage.getItem(REFRESH_TOKEN_KEY);

    if (refreshToken) {
      // Acquire mutex to prevent multiple refresh attempts
      if (!mutex.isLocked()) {
        const release = await mutex.acquire();
        try {
          // Try to refresh the token
          const refreshResult = await baseQuery(
            {
              url: "/auth/refresh",
              method: "POST",
              body: { token: refreshToken },
            },
            api,
            extraOptions
          );

          if (refreshResult.data) {
            // Store the new token
            const { token, refreshToken: newRefreshToken } =
              refreshResult.data as {
                token: string;
                refreshToken?: string;
              };

            // Update auth state
            api.dispatch({
              type: "auth/updateTokens",
              payload: { token, refreshToken: newRefreshToken },
            });

            // Store in localStorage
            localStorage.setItem(AUTH_TOKEN_KEY, token);
            if (newRefreshToken) {
              localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
            }

            // Retry the original request with new token
            result = await staggeredBaseQuery(args, api, extraOptions);
          } else {
            // Refresh failed - logout user
            console.error("Token refresh failed");
            api.dispatch({ type: "auth/clearAuth" });
          }
        } finally {
          release();
        }
      } else {
        // Another request is refreshing - wait and retry
        await mutex.waitForUnlock();
        result = await staggeredBaseQuery(args, api, extraOptions);
      }
    } else {
      // No refresh token available - logout
      console.error("No refresh token available");
      api.dispatch({ type: "auth/clearAuth" });
    }
  }

  // Log errors in development
  if (result.error && import.meta.env.DEV) {
    const { status, data } = result.error;
    console.error(`API Error ${status}:`, data || "Unknown error");
  }

  return result;
};

export const apiSlice = createApi({
  reducerPath: "api",
  baseQuery: baseQueryWithReauth,
  // Keep unused data in cache for 60 seconds
  keepUnusedDataFor: 60,
  // Refetch on reconnect and focus
  refetchOnReconnect: true,
  refetchOnFocus: false, // Disable to prevent unnecessary refetches
  tagTypes: [
    "User",
    "Member",
    "Loan",
    "LoanBalance",
    "Collateral",
    "Guarantor",
    "SavingAccount",
    "SavingTransaction",
    "SavingBalance",
    "Payment",
    "Dashboard",
  ],
  endpoints: () => ({}),
});
