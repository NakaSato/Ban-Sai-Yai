import { useState, useCallback, useRef, useEffect } from "react";
import { useDispatch } from "react-redux";
import { showNotification } from "@/store/slices/uiSlice";

interface UseAsyncOperationOptions {
  showSuccessNotification?: boolean;
  showErrorNotification?: boolean;
  successMessage?: string;
  errorMessage?: string;
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

interface AsyncOperationState {
  isLoading: boolean;
  error: Error | null;
  data: any;
}

export const useAsyncOperation = <T = any>(
  options: UseAsyncOperationOptions = {}
) => {
  const [state, setState] = useState<AsyncOperationState>({
    isLoading: false,
    error: null,
    data: null,
  });

  const dispatch = useDispatch();
  const abortControllerRef = useRef<AbortController | null>(null);

  const execute = useCallback(
    async (asyncFunction: () => Promise<T>): Promise<T | null> => {
      // Cancel any previous operation
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }

      // Create new abort controller
      abortControllerRef.current = new AbortController();

      setState({ isLoading: true, error: null, data: null });

      try {
        const result = await asyncFunction();

        setState({ isLoading: false, error: null, data: result });

        // Show success notification if enabled
        if (options.showSuccessNotification) {
          dispatch(
            showNotification({
              message:
                options.successMessage || "Operation completed successfully",
              severity: "success",
            })
          );
        }

        // Call success callback
        options.onSuccess?.();

        return result;
      } catch (error) {
        const errorObj =
          error instanceof Error ? error : new Error(String(error));

        setState({ isLoading: false, error: errorObj, data: null });

        // Show error notification if enabled
        if (options.showErrorNotification !== false) {
          dispatch(
            showNotification({
              message:
                options.errorMessage || errorObj.message || "An error occurred",
              severity: "error",
            })
          );
        }

        // Call error callback
        options.onError?.(errorObj);

        return null;
      }
    },
    [dispatch, options]
  );

  const reset = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
    setState({ isLoading: false, error: null, data: null });
  }, []);

  const clearError = useCallback(() => {
    setState((prev) => ({ ...prev, error: null }));
  }, []);

  return {
    ...state,
    execute,
    reset,
    clearError,
  };
};

// Hook for handling form submissions
export const useFormSubmission = <T = any>(
  submitFunction: (data: T) => Promise<any>,
  options: UseAsyncOperationOptions = {}
) => {
  const { execute, isLoading, error, reset, clearError } =
    useAsyncOperation(options);

  const handleSubmit = useCallback(
    async (data: T) => {
      return execute(() => submitFunction(data));
    },
    [execute, submitFunction]
  );

  return {
    handleSubmit,
    isLoading,
    error,
    reset,
    clearError,
  };
};

// Hook for data fetching with caching
export const useDataFetcher = <T = any>(
  fetchFunction: () => Promise<T>,
  options: {
    cacheKey?: string;
    cacheTime?: number;
    refetchOnWindowFocus?: boolean;
  } & UseAsyncOperationOptions = {}
) => {
  const [data, setData] = useState<T | null>(null);
  const [lastFetch, setLastFetch] = useState<number>(0);

  const { execute, isLoading, error } = useAsyncOperation(options);

  const fetchData = useCallback(
    async (forceRefetch = false) => {
      const now = Date.now();
      const cacheTime = options.cacheTime || 5 * 60 * 1000; // 5 minutes default

      // Check if we should use cached data
      if (
        !forceRefetch &&
        data &&
        options.cacheKey &&
        now - lastFetch < cacheTime
      ) {
        return data;
      }

      const result = await execute(fetchFunction);
      if (result) {
        setData(result);
        setLastFetch(now);
      }
      return result;
    },
    [
      execute,
      fetchFunction,
      data,
      lastFetch,
      options.cacheTime,
      options.cacheKey,
    ]
  );

  // Refetch on window focus if enabled
  useEffect(() => {
    if (options.refetchOnWindowFocus) {
      const handleFocus = () => fetchData(true);
      window.addEventListener("focus", handleFocus);
      return () => window.removeEventListener("focus", handleFocus);
    }
  }, [fetchData, options.refetchOnWindowFocus]);

  return {
    data,
    isLoading,
    error,
    fetchData,
    refetch: () => fetchData(true),
  };
};

export default useAsyncOperation;
