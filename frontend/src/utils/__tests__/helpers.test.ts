import {
  formatCurrency,
  formatNumber,
  formatPercent,
  formatDate,
  formatDateShort,
  formatDateTime,
  formatRelativeTime,
  formatFileSize,
  getFullName,
  getInitials,
  truncateText,
  capitalize,
  enumToLabel,
  getStatusColor,
  debounce,
  throttle,
  deepClone,
  isEmpty,
  generateId,
  parseQueryString,
  buildQueryString,
  calculateMonthlyPayment,
  calculateTotalInterest,
} from "../helpers";

describe("helpers utilities", () => {
  describe("formatCurrency", () => {
    it("should format numbers as Thai Baht", () => {
      expect(formatCurrency(1000)).toContain("1,000");
      expect(formatCurrency(1234.56)).toContain("1,234.56");
    });

    it("should handle null/undefined", () => {
      expect(formatCurrency(null)).toBe("฿0.00");
      expect(formatCurrency(undefined)).toBe("฿0.00");
    });

    it("should handle string numbers", () => {
      expect(formatCurrency("1000")).toContain("1,000");
    });

    it("should handle NaN", () => {
      expect(formatCurrency("invalid")).toBe("฿0.00");
    });
  });

  describe("formatNumber", () => {
    it("should format numbers with Thai locale", () => {
      expect(formatNumber(1000)).toBe("1,000");
      expect(formatNumber(1234567)).toBe("1,234,567");
    });

    it("should respect decimals parameter", () => {
      expect(formatNumber(1234.567, 2)).toBe("1,234.57");
    });

    it("should handle null/undefined", () => {
      expect(formatNumber(null)).toBe("0");
      expect(formatNumber(undefined)).toBe("0");
    });
  });

  describe("formatPercent", () => {
    it("should format percentages", () => {
      expect(formatPercent(12.345)).toBe("12.35%");
      expect(formatPercent(100)).toBe("100.00%");
    });

    it("should respect decimals parameter", () => {
      expect(formatPercent(12.345, 1)).toBe("12.3%");
    });

    it("should handle null/undefined", () => {
      expect(formatPercent(null)).toBe("0%");
      expect(formatPercent(undefined)).toBe("0%");
    });
  });

  describe("formatDate", () => {
    it("should format dates in Thai locale", () => {
      const result = formatDate("2024-01-15");
      expect(result).not.toBe("-");
    });

    it("should handle Date objects", () => {
      const result = formatDate(new Date("2024-01-15"));
      expect(result).not.toBe("-");
    });

    it("should handle null/undefined", () => {
      expect(formatDate(null)).toBe("-");
      expect(formatDate(undefined)).toBe("-");
    });

    it("should handle invalid dates", () => {
      expect(formatDate("invalid")).toBe("-");
    });
  });

  describe("formatDateShort", () => {
    it("should format dates in short format", () => {
      const result = formatDateShort("2024-01-15");
      expect(result).toMatch(/\d{2}\/\d{2}\/\d{4}/);
    });

    it("should handle null/undefined", () => {
      expect(formatDateShort(null)).toBe("-");
    });
  });

  describe("formatDateTime", () => {
    it("should format datetime", () => {
      const result = formatDateTime("2024-01-15T10:30:00");
      expect(result).not.toBe("-");
    });

    it("should handle null/undefined", () => {
      expect(formatDateTime(null)).toBe("-");
    });
  });

  describe("formatRelativeTime", () => {
    it("should return relative time for recent dates", () => {
      const now = new Date();
      const fiveMinutesAgo = new Date(now.getTime() - 5 * 60 * 1000);
      const result = formatRelativeTime(fiveMinutesAgo);
      expect(result).toContain("นาที");
    });

    it("should handle null/undefined", () => {
      expect(formatRelativeTime(null)).toBe("-");
    });
  });

  describe("formatFileSize", () => {
    it("should format bytes correctly", () => {
      expect(formatFileSize(0)).toBe("0 Bytes");
      expect(formatFileSize(1024)).toBe("1 KB");
      expect(formatFileSize(1048576)).toBe("1 MB");
      expect(formatFileSize(1073741824)).toBe("1 GB");
    });
  });

  describe("getFullName", () => {
    it("should combine first and last name", () => {
      expect(getFullName("John", "Doe")).toBe("John Doe");
    });

    it("should handle missing parts", () => {
      expect(getFullName("John", null)).toBe("John");
      expect(getFullName(null, "Doe")).toBe("Doe");
      expect(getFullName(null, null)).toBe("-");
    });
  });

  describe("getInitials", () => {
    it("should get initials from names", () => {
      expect(getInitials("John", "Doe")).toBe("JD");
    });

    it("should handle missing parts", () => {
      expect(getInitials("John", null)).toBe("J");
      expect(getInitials(null, null)).toBe("?");
    });
  });

  describe("truncateText", () => {
    it("should truncate long text", () => {
      expect(truncateText("Hello World", 5)).toBe("Hello...");
    });

    it("should not truncate short text", () => {
      expect(truncateText("Hi", 5)).toBe("Hi");
    });

    it("should handle null/undefined", () => {
      expect(truncateText(null, 5)).toBe("");
    });
  });

  describe("capitalize", () => {
    it("should capitalize first letter", () => {
      expect(capitalize("hello")).toBe("Hello");
      expect(capitalize("HELLO")).toBe("Hello");
    });

    it("should handle null/undefined", () => {
      expect(capitalize(null)).toBe("");
    });
  });

  describe("enumToLabel", () => {
    it("should convert enum to label", () => {
      expect(enumToLabel("LOAN_APPLICATION")).toBe("Loan Application");
      expect(enumToLabel("ACTIVE")).toBe("Active");
    });

    it("should handle null/undefined", () => {
      expect(enumToLabel(null)).toBe("-");
    });
  });

  describe("getStatusColor", () => {
    it("should return correct colors for statuses", () => {
      expect(getStatusColor("ACTIVE")).toBe("success");
      expect(getStatusColor("PENDING")).toBe("warning");
      expect(getStatusColor("REJECTED")).toBe("error");
      expect(getStatusColor("UNKNOWN")).toBe("default");
    });
  });

  describe("debounce", () => {
    jest.useFakeTimers();

    it("should debounce function calls", () => {
      const fn = jest.fn();
      const debounced = debounce(fn, 100);

      debounced();
      debounced();
      debounced();

      expect(fn).not.toHaveBeenCalled();

      jest.advanceTimersByTime(100);

      expect(fn).toHaveBeenCalledTimes(1);
    });
  });

  describe("throttle", () => {
    jest.useFakeTimers();

    it("should throttle function calls", () => {
      const fn = jest.fn();
      const throttled = throttle(fn, 100);

      throttled();
      throttled();
      throttled();

      expect(fn).toHaveBeenCalledTimes(1);

      jest.advanceTimersByTime(100);
      throttled();

      expect(fn).toHaveBeenCalledTimes(2);
    });
  });

  describe("deepClone", () => {
    it("should deep clone objects", () => {
      const obj = { a: 1, b: { c: 2 } };
      const clone = deepClone(obj);

      expect(clone).toEqual(obj);
      expect(clone).not.toBe(obj);
      expect(clone.b).not.toBe(obj.b);
    });

    it("should handle primitives", () => {
      expect(deepClone(5)).toBe(5);
      expect(deepClone("hello")).toBe("hello");
      expect(deepClone(null)).toBe(null);
    });
  });

  describe("isEmpty", () => {
    it("should detect empty objects", () => {
      expect(isEmpty({})).toBe(true);
      expect(isEmpty({ a: 1 })).toBe(false);
    });

    it("should handle null/undefined", () => {
      expect(isEmpty(null)).toBe(true);
      expect(isEmpty(undefined)).toBe(true);
    });
  });

  describe("generateId", () => {
    it("should generate unique IDs", () => {
      const id1 = generateId();
      const id2 = generateId();

      expect(id1).toBeTruthy();
      expect(id2).toBeTruthy();
      expect(id1).not.toBe(id2);
    });
  });

  describe("parseQueryString", () => {
    it("should parse query strings", () => {
      const result = parseQueryString("?page=1&size=10");
      expect(result).toEqual({ page: "1", size: "10" });
    });

    it("should handle empty query string", () => {
      expect(parseQueryString("")).toEqual({});
    });
  });

  describe("buildQueryString", () => {
    it("should build query strings", () => {
      const result = buildQueryString({ page: 1, size: 10 });
      expect(result).toBe("page=1&size=10");
    });

    it("should skip null/undefined values", () => {
      const result = buildQueryString({ page: 1, empty: null, blank: "" });
      expect(result).toBe("page=1");
    });
  });

  describe("calculateMonthlyPayment", () => {
    it("should calculate monthly payment correctly", () => {
      // $10,000 at 12% annual rate for 12 months
      const payment = calculateMonthlyPayment(10000, 12, 12);
      expect(payment).toBeCloseTo(888.49, 1);
    });

    it("should handle zero interest", () => {
      const payment = calculateMonthlyPayment(12000, 0, 12);
      expect(payment).toBe(1000);
    });

    it("should handle invalid inputs", () => {
      expect(calculateMonthlyPayment(0, 12, 12)).toBe(0);
      expect(calculateMonthlyPayment(10000, 12, 0)).toBe(0);
    });
  });

  describe("calculateTotalInterest", () => {
    it("should calculate total interest correctly", () => {
      const interest = calculateTotalInterest(10000, 12, 12);
      expect(interest).toBeGreaterThan(0);
    });

    it("should return 0 for zero interest rate", () => {
      const interest = calculateTotalInterest(10000, 0, 12);
      expect(interest).toBe(0);
    });
  });
});
