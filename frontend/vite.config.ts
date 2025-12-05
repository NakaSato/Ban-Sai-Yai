import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@/components": path.resolve(__dirname, "./src/components"),
      "@/pages": path.resolve(__dirname, "./src/pages"),
      "@/store": path.resolve(__dirname, "./src/store"),
      "@/services": path.resolve(__dirname, "./src/services"),
      "@/hooks": path.resolve(__dirname, "./src/hooks"),
      "@/utils": path.resolve(__dirname, "./src/utils"),
      "@/types": path.resolve(__dirname, "./src/types"),
      "@/constants": path.resolve(__dirname, "./src/constants"),
    },
  },
  server: {
    port: 3001,
    proxy: {
      "/api": {
        target: "http://localhost:9090",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    // Set chunk size warning limit to 500 KB
    chunkSizeWarningLimit: 500,

    // Enable source maps for production debugging
    sourcemap: "hidden",

    // Configure terser minification with optimal compression settings
    minify: "terser",
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ["console.log", "console.info", "console.debug"],
        passes: 2,
      },
      mangle: {
        safari10: true,
      },
      format: {
        comments: false,
      },
    },

    rollupOptions: {
      output: {
        // Configure manual chunks for vendor libraries
        manualChunks: (id: string) => {
          // React ecosystem chunk
          if (
            id.includes("node_modules/react") ||
            id.includes("node_modules/react-dom") ||
            id.includes("node_modules/react-router-dom") ||
            id.includes("node_modules/react-router") ||
            id.includes("node_modules/scheduler")
          ) {
            return "react-vendor";
          }

          // Material-UI core chunk (split into multiple chunks for better size management)
          if (id.includes("node_modules/@mui/material")) {
            return "mui-vendor";
          }

          // Material-UI data grid chunk (separate due to size)
          if (id.includes("node_modules/@mui/x-data-grid")) {
            return "mui-datagrid-vendor";
          }

          // Material-UI date pickers chunk (separate due to size)
          if (id.includes("node_modules/@mui/x-date-pickers")) {
            return "mui-datepickers-vendor";
          }

          // Material-UI icons chunk (separate due to size)
          if (id.includes("node_modules/@mui/icons-material")) {
            return "mui-icons-vendor";
          }

          // Emotion styling library chunk
          if (id.includes("node_modules/@emotion")) {
            return "emotion-vendor";
          }

          // Other MUI packages
          if (id.includes("node_modules/@mui")) {
            return "mui-other-vendor";
          }

          // Charts chunk
          if (
            id.includes("node_modules/chart.js") ||
            id.includes("node_modules/react-chartjs-2") ||
            id.includes("node_modules/recharts")
          ) {
            return "charts-vendor";
          }

          // State management chunk
          if (
            id.includes("node_modules/@reduxjs/toolkit") ||
            id.includes("node_modules/react-redux") ||
            id.includes("node_modules/redux")
          ) {
            return "state-vendor";
          }

          // Utilities chunk
          if (
            id.includes("node_modules/axios") ||
            id.includes("node_modules/date-fns") ||
            id.includes("node_modules/yup") ||
            id.includes("node_modules/react-hook-form") ||
            id.includes("node_modules/@hookform/resolvers")
          ) {
            return "utils-vendor";
          }

          // Return undefined for application code to allow automatic chunking
          return undefined;
        },

        // Configure file naming patterns with content hashes for cache busting
        chunkFileNames: "assets/[name]-[hash].js",
        entryFileNames: "assets/[name]-[hash].js",
        assetFileNames: "assets/[name]-[hash].[ext]",
      },
    },
  },
});
