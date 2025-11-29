import React from "react";
import { screen, fireEvent } from "@testing-library/react";
import ErrorBoundary from "../ErrorBoundary";

// Component that throws an error for testing
const ThrowErrorComponent: React.FC<{ shouldThrow: boolean }> = ({
  shouldThrow,
}) => {
  if (shouldThrow) {
    throw new Error("Test error");
  }
  return <div data-testid="no-error">No error</div>;
};

describe("ErrorBoundary", () => {
  beforeEach(() => {
    jest.spyOn(console, "error").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it("should render children when there is no error", () => {
    render(
      <ErrorBoundary>
        <ThrowErrorComponent shouldThrow={false} />
      </ErrorBoundary>
    );

    expect(screen.getByTestId("no-error")).toBeInTheDocument();
    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
  });

  it("should catch and display error when child component throws", () => {
    render(
      <ErrorBoundary>
        <ThrowErrorComponent shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(screen.getByText("Something went wrong")).toBeInTheDocument();
    expect(
      screen.getByText(
        "An unexpected error occurred while rendering this component."
      )
    ).toBeInTheDocument();
  });

  it("should display error details in development mode", () => {
    const originalEnv = import.meta.env.DEV;
    import.meta.env.DEV = true;

    render(
      <ErrorBoundary>
        <ThrowErrorComponent shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(
      screen.getByText("Error Details (Development Only):")
    ).toBeInTheDocument();
    expect(screen.getByText(/Test error/)).toBeInTheDocument();

    import.meta.env.DEV = originalEnv;
  });

  it("should reset error state when reset button is clicked", () => {
    const { rerender } = render(
      <ErrorBoundary>
        <ThrowErrorComponent shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(screen.getByText("Something went wrong")).toBeInTheDocument();

    const resetButton = screen.getByText("Try Again");
    fireEvent.click(resetButton);

    rerender(
      <ErrorBoundary>
        <ThrowErrorComponent shouldThrow={false} />
      </ErrorBoundary>
    );

    expect(screen.getByTestId("no-error")).toBeInTheDocument();
    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
  });

  it("should render custom fallback when provided", () => {
    const customFallback = (
      <div data-testid="custom-fallback">Custom Error</div>
    );

    render(
      <ErrorBoundary fallback={customFallback}>
        <ThrowErrorComponent shouldThrow={true} />
      </ErrorBoundary>
    );

    expect(screen.getByTestId("custom-fallback")).toBeInTheDocument();
    expect(screen.queryByText("Something went wrong")).not.toBeInTheDocument();
  });
});
