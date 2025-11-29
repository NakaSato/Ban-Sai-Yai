import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Switch,
  FormControlLabel,
  Paper,
} from "@mui/material";

interface PerformanceMetrics {
  fps: number;
  memory: number;
  renderTime: number;
}

export const PerformanceMonitor = ({
  enabled,
  onToggle,
}: {
  enabled: boolean;
  onToggle: (enabled: boolean) => void;
}) => {
  const [metrics, setMetrics] = useState<PerformanceMetrics>({
    fps: 0,
    memory: 0,
    renderTime: 0,
  });

  useEffect(() => {
    if (!enabled) return;

    let frameCount = 0;
    let lastTime = performance.now();
    let animationId: number;

    const measureFPS = () => {
      frameCount++;
      const currentTime = performance.now();

      if (currentTime >= lastTime + 1000) {
        const fps = Math.round((frameCount * 1000) / (currentTime - lastTime));

        setMetrics((prev) => ({
          ...prev,
          fps,
          memory: (performance as any).memory
            ? Math.round((performance as any).memory.usedJSHeapSize / 1048576)
            : 0,
          renderTime: currentTime - lastTime,
        }));

        frameCount = 0;
        lastTime = currentTime;
      }

      animationId = requestAnimationFrame(measureFPS);
    };

    animationId = requestAnimationFrame(measureFPS);

    return () => {
      if (animationId) {
        cancelAnimationFrame(animationId);
      }
    };
  }, [enabled]);

  if (!enabled) return null;

  return (
    <Paper
      sx={{
        position: "fixed",
        top: 16,
        right: 16,
        p: 2,
        zIndex: 9999,
        backgroundColor: "rgba(0, 0, 0, 0.8)",
        color: "white",
        fontSize: "0.875rem",
        minWidth: 200,
      }}
    >
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={1}
      >
        <Typography variant="subtitle2">Performance</Typography>
        <FormControlLabel
          control={
            <Switch
              checked={enabled}
              onChange={(e) => onToggle(e.target.checked)}
            />
          }
          label=""
        />
      </Box>

      <Typography variant="body2">FPS: {metrics.fps}</Typography>
      <Typography variant="body2">Memory: {metrics.memory} MB</Typography>
      <Typography variant="body2">
        Render: {metrics.renderTime.toFixed(2)} ms
      </Typography>
    </Paper>
  );
};

// Hook for performance monitoring
export const usePerformanceMonitor = () => {
  const [isEnabled, setIsEnabled] = useState(import.meta.env.DEV);

  const startProfiling = () => {
    console.log("Performance profiling started");
    if (performance.mark) {
      performance.mark("profile-start");
    }
  };

  const endProfiling = (name: string) => {
    if (performance.mark && performance.measure) {
      performance.mark("profile-end");
      performance.measure(name, "profile-start", "profile-end");

      const measures = performance.getEntriesByName(name, "measure");
      const lastMeasure = measures[measures.length - 1];

      if (lastMeasure) {
        console.log(
          `Performance: ${name} took ${lastMeasure.duration.toFixed(2)}ms`
        );
      }
    }
  };

  return {
    isEnabled,
    setIsEnabled,
    startProfiling,
    endProfiling,
  };
};

export default PerformanceMonitor;
