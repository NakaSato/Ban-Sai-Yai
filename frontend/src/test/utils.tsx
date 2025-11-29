import React, { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";

import { store } from "@/store";
import { getTheme } from "@/theme";

// Custom render function with providers
const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
  const theme = getTheme("light");

  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <BrowserRouter>{children}</BrowserRouter>
        </LocalizationProvider>
      </ThemeProvider>
    </Provider>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, "wrapper">
) => render(ui, { wrapper: AllTheProviders, ...options });

// Re-export everything from React Testing Library
export * from "@testing-library/react";
export { customRender as render };

// Mock store for testing
export const createMockStore = (initialState = {}) => {
  return {
    ...store,
    getState: () => ({
      auth: {
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      },
      ui: {
        theme: "light",
        sidebarCollapsed: false,
        notifications: [],
      },
      ...initialState,
    }),
    dispatch: jest.fn(),
    subscribe: jest.fn(),
    replaceReducer: jest.fn(),
  };
};

// Mock user data
export const mockUser = {
  id: "1",
  username: "testuser",
  email: "test@example.com",
  firstName: "Test",
  lastName: "User",
  role: "MEMBER",
  isActive: true,
  createdAt: "2023-01-01T00:00:00.000Z",
  updatedAt: "2023-01-01T00:00:00.000Z",
};

// Mock component props
export const mockProps = {
  className: "test-class",
  "data-testid": "test-component",
};
