import { useState, useEffect, useRef, RefObject } from "react";

interface UseIntersectionObserverOptions {
  root?: Element | null;
  rootMargin?: string;
  threshold?: number | number[];
  freezeOnceVisible?: boolean;
}

/**
 * Hook for detecting when an element is visible in the viewport
 * Useful for lazy loading, infinite scroll, analytics tracking
 */
export function useIntersectionObserver(
  options: UseIntersectionObserverOptions = {}
): [RefObject<HTMLDivElement>, boolean, IntersectionObserverEntry | undefined] {
  const {
    root = null,
    rootMargin = "0px",
    threshold = 0,
    freezeOnceVisible = false,
  } = options;

  const elementRef = useRef<HTMLDivElement>(null);
  const [isVisible, setIsVisible] = useState(false);
  const [entry, setEntry] = useState<IntersectionObserverEntry>();

  const frozen = entry?.isIntersecting && freezeOnceVisible;

  useEffect(() => {
    const element = elementRef.current;
    if (!element || frozen) return;

    const observer = new IntersectionObserver(
      ([observerEntry]) => {
        setEntry(observerEntry);
        setIsVisible(observerEntry.isIntersecting);
      },
      { root, rootMargin, threshold }
    );

    observer.observe(element);

    return () => {
      observer.disconnect();
    };
  }, [root, rootMargin, threshold, frozen]);

  return [elementRef, isVisible, entry];
}

/**
 * Hook for infinite scrolling
 * Triggers callback when sentinel element becomes visible
 */
export function useInfiniteScroll(
  callback: () => void,
  options: { enabled?: boolean; rootMargin?: string } = {}
): RefObject<HTMLDivElement> {
  const { enabled = true, rootMargin = "100px" } = options;
  const [ref, isVisible] = useIntersectionObserver({ rootMargin });

  useEffect(() => {
    if (isVisible && enabled) {
      callback();
    }
  }, [isVisible, enabled, callback]);

  return ref;
}

export default useIntersectionObserver;
