import React, { forwardRef } from "react";
import {
  Button,
  ButtonProps,
  IconButton,
  IconButtonProps,
  Tooltip,
} from "@mui/material";

/**
 * Accessible Button with required aria-label for icon-only buttons
 */
interface AccessibleIconButtonProps extends IconButtonProps {
  "aria-label": string; // Make aria-label required
  tooltipTitle?: string;
}

export const AccessibleIconButton = forwardRef<
  HTMLButtonElement,
  AccessibleIconButtonProps
>(({ tooltipTitle, children, ...props }, ref) => {
  const button = (
    <IconButton ref={ref} {...props}>
      {children}
    </IconButton>
  );

  if (tooltipTitle) {
    return <Tooltip title={tooltipTitle}>{button}</Tooltip>;
  }

  return button;
});

AccessibleIconButton.displayName = "AccessibleIconButton";

/**
 * Button with loading state accessibility
 */
interface AccessibleButtonProps extends ButtonProps {
  loading?: boolean;
  loadingText?: string;
}

export const AccessibleButton = forwardRef<
  HTMLButtonElement,
  AccessibleButtonProps
>(
  (
    { loading, loadingText = "กำลังโหลด...", children, disabled, ...props },
    ref
  ) => {
    return (
      <Button
        ref={ref}
        {...props}
        disabled={disabled || loading}
        aria-busy={loading}
        aria-disabled={disabled || loading}
      >
        {loading ? loadingText : children}
      </Button>
    );
  }
);

AccessibleButton.displayName = "AccessibleButton";

/**
 * Skip navigation link for keyboard users
 */
interface SkipLinkProps {
  targetId: string;
  children?: React.ReactNode;
}

export function SkipLink({
  targetId,
  children = "ข้ามไปยังเนื้อหาหลัก",
}: SkipLinkProps) {
  return (
    <a
      href={`#${targetId}`}
      style={{
        position: "absolute",
        left: "-9999px",
        top: "auto",
        width: "1px",
        height: "1px",
        overflow: "hidden",
      }}
      onFocus={(e) => {
        e.currentTarget.style.position = "fixed";
        e.currentTarget.style.top = "0";
        e.currentTarget.style.left = "0";
        e.currentTarget.style.width = "auto";
        e.currentTarget.style.height = "auto";
        e.currentTarget.style.padding = "1rem";
        e.currentTarget.style.background = "#1976d2";
        e.currentTarget.style.color = "white";
        e.currentTarget.style.zIndex = "9999";
        e.currentTarget.style.overflow = "visible";
      }}
      onBlur={(e) => {
        e.currentTarget.style.position = "absolute";
        e.currentTarget.style.left = "-9999px";
        e.currentTarget.style.width = "1px";
        e.currentTarget.style.height = "1px";
        e.currentTarget.style.overflow = "hidden";
      }}
    >
      {children}
    </a>
  );
}

/**
 * Visually hidden element for screen readers
 */
interface VisuallyHiddenProps {
  children: React.ReactNode;
  as?: keyof JSX.IntrinsicElements;
}

export function VisuallyHidden({
  children,
  as: Component = "span",
}: VisuallyHiddenProps) {
  return (
    <Component
      style={{
        position: "absolute",
        width: "1px",
        height: "1px",
        padding: 0,
        margin: "-1px",
        overflow: "hidden",
        clip: "rect(0, 0, 0, 0)",
        whiteSpace: "nowrap",
        border: 0,
      }}
    >
      {children}
    </Component>
  );
}

/**
 * Live region for announcing dynamic content to screen readers
 */
interface LiveRegionProps {
  message: string;
  "aria-live"?: "polite" | "assertive" | "off";
  "aria-atomic"?: boolean;
}

export function LiveRegion({
  message,
  "aria-live": ariaLive = "polite",
  "aria-atomic": ariaAtomic = true,
}: LiveRegionProps) {
  return (
    <div
      role="status"
      aria-live={ariaLive}
      aria-atomic={ariaAtomic}
      style={{
        position: "absolute",
        width: "1px",
        height: "1px",
        padding: 0,
        margin: "-1px",
        overflow: "hidden",
        clip: "rect(0, 0, 0, 0)",
        whiteSpace: "nowrap",
        border: 0,
      }}
    >
      {message}
    </div>
  );
}

/**
 * Keyboard-navigable focus trap for modals and dialogs
 */
export function useFocusTrap(containerRef: React.RefObject<HTMLElement>) {
  React.useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const focusableElements = container.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );

    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key !== "Tab") return;

      if (e.shiftKey) {
        if (document.activeElement === firstElement) {
          e.preventDefault();
          lastElement?.focus();
        }
      } else {
        if (document.activeElement === lastElement) {
          e.preventDefault();
          firstElement?.focus();
        }
      }
    };

    container.addEventListener("keydown", handleKeyDown);
    firstElement?.focus();

    return () => {
      container.removeEventListener("keydown", handleKeyDown);
    };
  }, [containerRef]);
}

/**
 * HOC to add focus outline on keyboard navigation only
 */
export function useKeyboardFocusOutline() {
  React.useEffect(() => {
    const handleFirstTab = (e: KeyboardEvent) => {
      if (e.key === "Tab") {
        document.body.classList.add("user-is-tabbing");
        window.removeEventListener("keydown", handleFirstTab);
        window.addEventListener("mousedown", handleMouseDown);
      }
    };

    const handleMouseDown = () => {
      document.body.classList.remove("user-is-tabbing");
      window.removeEventListener("mousedown", handleMouseDown);
      window.addEventListener("keydown", handleFirstTab);
    };

    window.addEventListener("keydown", handleFirstTab);

    return () => {
      window.removeEventListener("keydown", handleFirstTab);
      window.removeEventListener("mousedown", handleMouseDown);
    };
  }, []);
}

export default {
  AccessibleIconButton,
  AccessibleButton,
  SkipLink,
  VisuallyHidden,
  LiveRegion,
  useFocusTrap,
  useKeyboardFocusOutline,
};
