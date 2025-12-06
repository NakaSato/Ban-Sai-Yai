import {
  isFetchBaseQueryError,
  isSerializedError,
  parseApiError,
  getErrorMessage,
  isAuthenticationError,
  formatValidationErrors,
} from "../errorHandler";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";
import { SerializedError } from "@reduxjs/toolkit";

describe("errorHandler utilities", () => {
  describe("isFetchBaseQueryError", () => {
    it("should return true for FetchBaseQueryError", () => {
      const error = { status: 400, data: { message: "Bad request" } };
      expect(isFetchBaseQueryError(error)).toBe(true);
    });

    it("should return false for non-FetchBaseQueryError", () => {
      expect(isFetchBaseQueryError(null)).toBe(false);
      expect(isFetchBaseQueryError(undefined)).toBe(false);
      expect(isFetchBaseQueryError({ message: "error" })).toBe(false);
    });
  });

  describe("isSerializedError", () => {
    it("should return true for SerializedError", () => {
      const error = { message: "Error occurred", name: "Error" };
      expect(isSerializedError(error)).toBe(true);
    });

    it("should return false for non-SerializedError", () => {
      expect(isSerializedError(null)).toBe(false);
      expect(isSerializedError(undefined)).toBe(false);
    });
  });

  describe("parseApiError", () => {
    it("should parse FetchBaseQueryError with status code", () => {
      const error: FetchBaseQueryError = {
        status: 401,
        data: { message: "Unauthorized" },
      };
      const result = parseApiError(error);

      expect(result.status).toBe(401);
      expect(result.isAuthError).toBe(true);
      expect(result.message).toBeTruthy();
    });

    it("should parse network errors", () => {
      const error: FetchBaseQueryError = {
        status: "FETCH_ERROR",
        error: "Network error",
      };
      const result = parseApiError(error);

      expect(result.isNetworkError).toBe(true);
      expect(result.message).toContain("connect");
    });

    it("should parse validation errors (400)", () => {
      const error: FetchBaseQueryError = {
        status: 400,
        data: {
          message: "Validation failed",
          error: { field: "email", details: "Invalid email" },
        },
      };
      const result = parseApiError(error);

      expect(result.isValidationError).toBe(true);
      expect(result.field).toBe("email");
    });

    it("should parse server errors (500)", () => {
      const error: FetchBaseQueryError = {
        status: 500,
        data: null,
      };
      const result = parseApiError(error);

      expect(result.isServerError).toBe(true);
    });

    it("should handle SerializedError", () => {
      const error: SerializedError = {
        message: "Something went wrong",
        name: "Error",
        code: "ERR_001",
      };
      const result = parseApiError(error);

      expect(result.message).toBe("Something went wrong");
      expect(result.code).toBe("ERR_001");
    });

    it("should return default error for undefined", () => {
      const result = parseApiError(undefined);

      expect(result.message).toBe("An unexpected error occurred.");
      expect(result.isNetworkError).toBe(false);
      expect(result.isAuthError).toBe(false);
    });
  });

  describe("getErrorMessage", () => {
    it("should extract error message", () => {
      const error: FetchBaseQueryError = {
        status: 404,
        data: { message: "Not found" },
      };
      const message = getErrorMessage(error);

      expect(message).toBe("Not found");
    });

    it("should return default message for undefined", () => {
      const message = getErrorMessage(undefined);
      expect(message).toBeTruthy();
    });
  });

  describe("isAuthenticationError", () => {
    it("should return true for 401 errors", () => {
      const error: FetchBaseQueryError = { status: 401, data: null };
      expect(isAuthenticationError(error)).toBe(true);
    });

    it("should return true for 403 errors", () => {
      const error: FetchBaseQueryError = { status: 403, data: null };
      expect(isAuthenticationError(error)).toBe(true);
    });

    it("should return false for other errors", () => {
      const error: FetchBaseQueryError = { status: 400, data: null };
      expect(isAuthenticationError(error)).toBe(false);
    });
  });

  describe("formatValidationErrors", () => {
    it("should format field-level errors", () => {
      const error: FetchBaseQueryError = {
        status: 400,
        data: {
          message: "Validation failed",
          error: { field: "email", details: "Invalid email format" },
        },
      };
      const errors = formatValidationErrors(error);

      expect(errors.email).toBe("Invalid email format");
    });

    it("should return empty object for non-validation errors", () => {
      const errors = formatValidationErrors(undefined);
      expect(errors).toEqual({});
    });
  });
});
