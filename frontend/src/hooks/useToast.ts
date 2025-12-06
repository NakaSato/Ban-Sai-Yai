import { useCallback } from "react";
import { useAppDispatch } from "@/hooks/redux";
import { showNotification, hideNotification } from "@/store/slices/uiSlice";
import { parseApiError, getErrorMessage } from "@/utils/errorHandler";
import type { FetchBaseQueryError } from "@reduxjs/toolkit/query";
import type { SerializedError } from "@reduxjs/toolkit";

type NotificationSeverity = "success" | "error" | "warning" | "info";

interface ToastOptions {
  duration?: number;
}

/**
 * Hook for showing toast notifications
 */
export function useToast() {
  const dispatch = useAppDispatch();

  const show = useCallback(
    (
      message: string,
      severity: NotificationSeverity = "info",
      options?: ToastOptions
    ) => {
      dispatch(showNotification({ message, severity }));

      // Auto-hide after duration (default: 5 seconds for success/info, 8 seconds for error/warning)
      const duration =
        options?.duration ??
        (severity === "error" || severity === "warning" ? 8000 : 5000);

      setTimeout(() => {
        dispatch(hideNotification());
      }, duration);
    },
    [dispatch]
  );

  const success = useCallback(
    (message: string, options?: ToastOptions) =>
      show(message, "success", options),
    [show]
  );

  const error = useCallback(
    (message: string, options?: ToastOptions) =>
      show(message, "error", options),
    [show]
  );

  const warning = useCallback(
    (message: string, options?: ToastOptions) =>
      show(message, "warning", options),
    [show]
  );

  const info = useCallback(
    (message: string, options?: ToastOptions) => show(message, "info", options),
    [show]
  );

  /**
   * Show error notification from API error
   */
  const apiError = useCallback(
    (
      apiError: FetchBaseQueryError | SerializedError | undefined,
      fallbackMessage?: string
    ) => {
      const message =
        getErrorMessage(apiError) || fallbackMessage || "An error occurred";
      show(message, "error");
    },
    [show]
  );

  /**
   * Show success notification for common actions
   */
  const saved = useCallback(
    (itemName?: string) =>
      success(
        itemName ? `${itemName} saved successfully` : "Saved successfully"
      ),
    [success]
  );

  const created = useCallback(
    (itemName?: string) =>
      success(
        itemName ? `${itemName} created successfully` : "Created successfully"
      ),
    [success]
  );

  const updated = useCallback(
    (itemName?: string) =>
      success(
        itemName ? `${itemName} updated successfully` : "Updated successfully"
      ),
    [success]
  );

  const deleted = useCallback(
    (itemName?: string) =>
      success(
        itemName ? `${itemName} deleted successfully` : "Deleted successfully"
      ),
    [success]
  );

  const hide = useCallback(() => {
    dispatch(hideNotification());
  }, [dispatch]);

  return {
    show,
    success,
    error,
    warning,
    info,
    apiError,
    saved,
    created,
    updated,
    deleted,
    hide,
  };
}

export default useToast;
