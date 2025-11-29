import {
  Box,
  CircularProgress,
  Typography,
  Backdrop,
  Paper,
} from "@mui/material";

interface LoadingSpinnerProps {
  size?: number;
  message?: string;
  fullScreen?: boolean;
  backdrop?: boolean;
  variant?: "circular" | "linear";
}

export const LoadingSpinner = ({
  size = 40,
  message,
  fullScreen = false,
  backdrop = false,
  variant = "circular",
}: LoadingSpinnerProps) => {
  const content = (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 2,
        ...(fullScreen && {
          minHeight: "100vh",
        }),
        ...(backdrop && {
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          zIndex: (theme) => theme.zIndex.modal,
        }),
      }}
    >
      {variant === "circular" ? (
        <CircularProgress size={size} />
      ) : (
        <Box sx={{ width: size * 2 }}>
          <CircularProgress variant="indeterminate" size={size * 2} />
        </Box>
      )}
      {message && (
        <Typography variant="body2" color="text.secondary" textAlign="center">
          {message}
        </Typography>
      )}
    </Box>
  );

  if (backdrop) {
    return (
      <Backdrop open sx={{ bgcolor: "rgba(255, 255, 255, 0.8)" }}>
        {content}
      </Backdrop>
    );
  }

  if (fullScreen) {
    return (
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          minHeight: "100vh",
        }}
      >
        <Paper
          sx={{
            p: 4,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: 2,
          }}
        >
          {content}
        </Paper>
      </Box>
    );
  }

  return content;
};

// Smaller inline loader for buttons and table cells
export const InlineLoader = ({ size = 20 }: { size?: number }) => {
  return <CircularProgress size={size} thickness={4} />;
};

// Skeleton loader for data tables
export const TableSkeleton = ({
  rows = 5,
  columns = 4,
}: {
  rows?: number;
  columns?: number;
}) => {
  return (
    <Box>
      {Array.from({ length: rows }).map((_, index) => (
        <Box
          key={index}
          sx={{
            display: "flex",
            gap: 2,
            p: 2,
            borderBottom: "1px solid",
            borderColor: "divider",
          }}
        >
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Box
              key={colIndex}
              sx={{
                flex: 1,
                height: 32,
                bgcolor: "grey.200",
                borderRadius: 1,
                animation: "pulse 1.5s ease-in-out infinite",
              }}
            />
          ))}
        </Box>
      ))}
    </Box>
  );
};

export default LoadingSpinner;
