import { useState, useCallback, useMemo } from "react";

interface UsePaginationOptions {
  initialPage?: number;
  initialPageSize?: number;
  totalItems?: number;
}

interface UsePaginationReturn {
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
  startIndex: number;
  endIndex: number;
  hasNextPage: boolean;
  hasPrevPage: boolean;
  isFirstPage: boolean;
  isLastPage: boolean;
  setPage: (page: number) => void;
  setPageSize: (size: number) => void;
  setTotalItems: (total: number) => void;
  nextPage: () => void;
  prevPage: () => void;
  firstPage: () => void;
  lastPage: () => void;
  resetPagination: () => void;
  paginationParams: { page: number; size: number };
}

/**
 * Hook for managing pagination state
 */
export function usePagination(
  options: UsePaginationOptions = {}
): UsePaginationReturn {
  const {
    initialPage = 0,
    initialPageSize = 10,
    totalItems: initialTotalItems = 0,
  } = options;

  const [page, setPageState] = useState(initialPage);
  const [pageSize, setPageSizeState] = useState(initialPageSize);
  const [totalItems, setTotalItems] = useState(initialTotalItems);

  const totalPages = useMemo(
    () => Math.max(1, Math.ceil(totalItems / pageSize)),
    [totalItems, pageSize]
  );

  const startIndex = useMemo(() => page * pageSize, [page, pageSize]);
  const endIndex = useMemo(
    () => Math.min(startIndex + pageSize - 1, totalItems - 1),
    [startIndex, pageSize, totalItems]
  );

  const hasNextPage = page < totalPages - 1;
  const hasPrevPage = page > 0;
  const isFirstPage = page === 0;
  const isLastPage = page >= totalPages - 1;

  const setPage = useCallback(
    (newPage: number) => {
      const validPage = Math.max(0, Math.min(newPage, totalPages - 1));
      setPageState(validPage);
    },
    [totalPages]
  );

  const setPageSize = useCallback((newSize: number) => {
    setPageSizeState(newSize);
    // Reset to first page when page size changes
    setPageState(0);
  }, []);

  const nextPage = useCallback(() => {
    if (hasNextPage) {
      setPageState((prev) => prev + 1);
    }
  }, [hasNextPage]);

  const prevPage = useCallback(() => {
    if (hasPrevPage) {
      setPageState((prev) => prev - 1);
    }
  }, [hasPrevPage]);

  const firstPage = useCallback(() => {
    setPageState(0);
  }, []);

  const lastPage = useCallback(() => {
    setPageState(totalPages - 1);
  }, [totalPages]);

  const resetPagination = useCallback(() => {
    setPageState(initialPage);
    setPageSizeState(initialPageSize);
  }, [initialPage, initialPageSize]);

  // Parameters ready to send to API
  const paginationParams = useMemo(
    () => ({ page, size: pageSize }),
    [page, pageSize]
  );

  return {
    page,
    pageSize,
    totalPages,
    totalItems,
    startIndex,
    endIndex,
    hasNextPage,
    hasPrevPage,
    isFirstPage,
    isLastPage,
    setPage,
    setPageSize,
    setTotalItems,
    nextPage,
    prevPage,
    firstPage,
    lastPage,
    resetPagination,
    paginationParams,
  };
}

export default usePagination;
