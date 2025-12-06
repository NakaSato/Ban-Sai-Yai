import { useState, useEffect } from "react";

type Breakpoint = "xs" | "sm" | "md" | "lg" | "xl";

interface UseMediaQueryReturn {
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  isLargeDesktop: boolean;
  breakpoint: Breakpoint;
  width: number;
  height: number;
}

// MUI default breakpoints
const BREAKPOINTS = {
  xs: 0,
  sm: 600,
  md: 900,
  lg: 1200,
  xl: 1536,
};

/**
 * Hook for responsive design - detects screen size and breakpoints
 */
export function useMediaQuery(): UseMediaQueryReturn {
  const [windowSize, setWindowSize] = useState({
    width: typeof window !== "undefined" ? window.innerWidth : 0,
    height: typeof window !== "undefined" ? window.innerHeight : 0,
  });

  useEffect(() => {
    const handleResize = () => {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    };

    window.addEventListener("resize", handleResize);
    handleResize(); // Call once to set initial size

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { width, height } = windowSize;

  const getBreakpoint = (): Breakpoint => {
    if (width >= BREAKPOINTS.xl) return "xl";
    if (width >= BREAKPOINTS.lg) return "lg";
    if (width >= BREAKPOINTS.md) return "md";
    if (width >= BREAKPOINTS.sm) return "sm";
    return "xs";
  };

  return {
    isMobile: width < BREAKPOINTS.sm,
    isTablet: width >= BREAKPOINTS.sm && width < BREAKPOINTS.md,
    isDesktop: width >= BREAKPOINTS.md && width < BREAKPOINTS.lg,
    isLargeDesktop: width >= BREAKPOINTS.lg,
    breakpoint: getBreakpoint(),
    width,
    height,
  };
}

/**
 * Hook for checking a specific media query
 */
export function useMatchMedia(query: string): boolean {
  const [matches, setMatches] = useState(() => {
    if (typeof window !== "undefined") {
      return window.matchMedia(query).matches;
    }
    return false;
  });

  useEffect(() => {
    const mediaQuery = window.matchMedia(query);

    const handleChange = (event: MediaQueryListEvent) => {
      setMatches(event.matches);
    };

    // Modern browsers
    mediaQuery.addEventListener("change", handleChange);

    // Set initial value
    setMatches(mediaQuery.matches);

    return () => {
      mediaQuery.removeEventListener("change", handleChange);
    };
  }, [query]);

  return matches;
}

/**
 * Hook to detect if user prefers reduced motion
 */
export function usePrefersReducedMotion(): boolean {
  return useMatchMedia("(prefers-reduced-motion: reduce)");
}

/**
 * Hook to detect if user prefers dark mode
 */
export function usePrefersDarkMode(): boolean {
  return useMatchMedia("(prefers-color-scheme: dark)");
}

export default useMediaQuery;
