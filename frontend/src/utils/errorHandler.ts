import { SerializedError } from "@reduxjs/toolkit";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";

/**
 * Error response structure from backend API
 */
export interface ApiErrorResponse {
  success: false;
  message: string;
  timestamp: string;
  requestId?: string;
  error?: {
    code: string;
    field?: string;
    details?: string;
  };
}

/**
 * Parsed error information for display
 */
export interface ParsedError {
  message: string;
  code?: string;
  field?: string;
  status?: number;
  isNetworkError: boolean;
  isAuthError: boolean;
  isValidationError: boolean;
  isServerError: boolean;
}

/**
 * Default error messages by status code
 */
const ERROR_MESSAGES: Record<number, string> = {
  400: "Invalid request. Please check your input.",
  401: "Your session has expired. Please login again.",
  403: "You do not have permission to perform this action.",
  404: "The requested resource was not found.",
  409: "A conflict occurred. The resource may already exist.",
  422: "Unable to process the request. Please check your data.",
  429: "Too many requests. Please try again later.",
  500: "An unexpected error occurred. Please try again later.",
  502: "Server is temporarily unavailable. Please try again.",
  503: "Service is currently unavailable. Please try again later.",
};

/**
 * Check if error is a FetchBaseQueryError
 */
export function isFetchBaseQueryError(
  error: unknown
): error is FetchBaseQueryError {
  return typeof error === "object" && error != null && "status" in error;
}

/**
 * Check if error is a SerializedError
 */
export function isSerializedError(error: unknown): error is SerializedError {
  return typeof error === "object" && error != null && "message" in error;
}

/**
 * Parse RTK Query error into a user-friendly format
 */
export function parseApiError(
  error: FetchBaseQueryError | SerializedError | undefined
): ParsedError {
  // Default error response
  const defaultError: ParsedError = {
    message: "An unexpected error occurred.",
    isNetworkError: false,
    isAuthError: false,
    isValidationError: false,
    isServerError: false,
  };

  if (!error) {
    return defaultError;
  }

  // Handle FetchBaseQueryError
  if (isFetchBaseQueryError(error)) {
    const status = typeof error.status === "number" ? error.status : undefined;

    // Network errors (FETCH_ERROR, PARSING_ERROR, etc.)
    if (typeof error.status === "string") {
      return {
        message:
          "Unable to connect to server. Please check your internet connection.",
        code: error.status,
        isNetworkError: true,
        isAuthError: false,
        isValidationError: false,
        isServerError: false,
      };
    }

    // Parse response data if available
    const data = error.data as ApiErrorResponse | undefined;
    const message =
      data?.message || ERROR_MESSAGES[status || 500] || defaultError.message;

    return {
      message,
      code: data?.error?.code,
      field: data?.error?.field,
      status,
      isNetworkError: false,
      isAuthError: status === 401 || status === 403,
      isValidationError: status === 400 || status === 422,
      isServerError: status !== undefined && status >= 500,
    };
  }

  // Handle SerializedError (from createAsyncThunk rejections)
  if (isSerializedError(error)) {
    return {
      message: error.message || defaultError.message,
      code: error.code,
      isNetworkError: false,
      isAuthError: false,
      isValidationError: false,
      isServerError: false,
    };
  }

  return defaultError;
}

/**
 * Get user-friendly error message from API error
 */
export function getErrorMessage(
  error: FetchBaseQueryError | SerializedError | undefined
): string {
  return parseApiError(error).message;
}

/**
 * Check if error requires re-authentication
 */
export function isAuthenticationError(
  error: FetchBaseQueryError | SerializedError | undefined
): boolean {
  return parseApiError(error).isAuthError;
}

/**
 * Format field-level validation errors for forms
 */
export function formatValidationErrors(
  error: FetchBaseQueryError | SerializedError | undefined
): Record<string, string> {
  const errors: Record<string, string> = {};

  if (!error || !isFetchBaseQueryError(error)) {
    return errors;
  }

  const data = error.data as ApiErrorResponse | undefined;

  if (data?.error?.field) {
    errors[data.error.field] = data.error.details || data.message;
  }

  return errors;
}

/**
 * Log error for debugging (only in development)
 */
export function logError(
  context: string,
  error: FetchBaseQueryError | SerializedError | unknown
): void {
  // Check if we're in development mode - compatible with both Vite and Jest
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const g = globalThis as any;
  const isDev =
    g.process?.env?.NODE_ENV !== "production" || g.import?.meta?.env?.DEV;
  if (isDev) {
    console.error(`[${context}] Error:`, error);
  }
}
