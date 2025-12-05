import React, { Component, Suspense, ReactNode } from 'react';
import { Box, Button, Typography, Alert } from '@mui/material';
import LoadingSpinner from './LoadingSpinner';

interface LazyComponentWrapperProps {
  children: ReactNode;
  fallback?: ReactNode;
  errorFallback?: ReactNode;
  onError?: (error: Error) => void;
}

interface LazyComponentWrapperState {
  hasError: boolean;
  error: Error | null;
  retryCount: number;
}

class ErrorBoundary extends Component<LazyComponentWrapperProps, LazyComponentWrapperState> {
  private maxRetries = 3;
  private retryTimeouts: number[] = [1000, 2000, 4000]; // Exponential backoff: 1s, 2s, 4s

  constructor(props: LazyComponentWrapperProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      retryCount: 0,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<LazyComponentWrapperState> {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    console.error('LazyComponentWrapper caught error:', error, errorInfo);
    
    if (this.props.onError) {
      this.props.onError(error);
    }

    // Auto-retry for chunk loading errors
    if (this.isChunkLoadError(error) && this.state.retryCount < this.maxRetries) {
      this.scheduleRetry();
    }
  }

  private isChunkLoadError(error: Error): boolean {
    return (
      error.name === 'ChunkLoadError' ||
      error.message.includes('Loading chunk') ||
      error.message.includes('Failed to fetch dynamically imported module')
    );
  }

  private scheduleRetry = (): void => {
    const { retryCount } = this.state;
    const delay = this.retryTimeouts[retryCount] || this.retryTimeouts[this.retryTimeouts.length - 1];

    setTimeout(() => {
      this.setState((prevState) => ({
        hasError: false,
        error: null,
        retryCount: prevState.retryCount + 1,
      }));
    }, delay);
  };

  private handleManualRetry = (): void => {
    this.setState({
      hasError: false,
      error: null,
      retryCount: 0,
    });
  };

  private handleReload = (): void => {
    window.location.reload();
  };

  render(): ReactNode {
    const { hasError, error, retryCount } = this.state;
    const { children, fallback, errorFallback } = this.props;

    if (hasError && error) {
      // Use custom error fallback if provided
      if (errorFallback) {
        return errorFallback;
      }

      // Check if we're still auto-retrying
      const isAutoRetrying = this.isChunkLoadError(error) && retryCount < this.maxRetries;

      if (isAutoRetrying) {
        return (
          <Box
            display="flex"
            flexDirection="column"
            alignItems="center"
            justifyContent="center"
            minHeight="200px"
            p={3}
          >
            <LoadingSpinner message={`Loading failed. Retrying (${retryCount}/${this.maxRetries})...`} />
          </Box>
        );
      }

      // Show error UI after all retries exhausted
      return (
        <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          minHeight="200px"
          p={3}
          gap={2}
        >
          <Alert severity="error" sx={{ maxWidth: 600 }}>
            <Typography variant="h6" gutterBottom>
              Failed to Load Component
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {this.isChunkLoadError(error)
                ? 'There was a problem loading this page. This might be due to a network issue or an outdated version of the application.'
                : 'An unexpected error occurred while loading this component.'}
            </Typography>
            {error.message && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                Error: {error.message}
              </Typography>
            )}
          </Alert>
          <Box display="flex" gap={2}>
            <Button variant="contained" color="primary" onClick={this.handleManualRetry}>
              Try Again
            </Button>
            <Button variant="outlined" color="secondary" onClick={this.handleReload}>
              Reload Page
            </Button>
          </Box>
        </Box>
      );
    }

    return (
      <Suspense fallback={fallback || <LoadingSpinner message="Loading..." />}>
        {children}
      </Suspense>
    );
  }
}

const LazyComponentWrapper: React.FC<LazyComponentWrapperProps> = (props) => {
  return <ErrorBoundary {...props} />;
};

export default LazyComponentWrapper;
