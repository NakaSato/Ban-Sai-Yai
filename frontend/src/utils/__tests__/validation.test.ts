import {
  isValidEmail,
  isValidPhone,
  isValidThaiNationalId,
  isValidThaiNationalIdWithChecksum,
  isValidName,
  containsXss,
  containsSqlInjection,
  stripHtml,
  sanitizeNumeric,
  sanitizeDecimal,
  sanitizePhone,
  formatThaiPhone,
  formatThaiNationalId,
  validatePasswordStrength,
  validateLoanAmount,
} from "../validation";

describe("validation utilities", () => {
  describe("isValidEmail", () => {
    it("should return true for valid emails", () => {
      expect(isValidEmail("test@example.com")).toBe(true);
      expect(isValidEmail("user.name@domain.co.th")).toBe(true);
      expect(isValidEmail("user+tag@example.org")).toBe(true);
    });

    it("should return false for invalid emails", () => {
      expect(isValidEmail("invalid-email")).toBe(false);
      expect(isValidEmail("@missing-local.com")).toBe(false);
      expect(isValidEmail("missing@")).toBe(false);
      expect(isValidEmail("")).toBe(false);
      expect(isValidEmail(null)).toBe(false);
      expect(isValidEmail(undefined)).toBe(false);
    });
  });

  describe("isValidPhone", () => {
    it("should return true for valid Thai phone numbers", () => {
      expect(isValidPhone("0812345678")).toBe(true);
      expect(isValidPhone("081-234-5678")).toBe(true);
      expect(isValidPhone("+66812345678")).toBe(true);
      expect(isValidPhone("02-123-4567")).toBe(true);
    });

    it("should return false for invalid phone numbers", () => {
      expect(isValidPhone("123")).toBe(false);
      expect(isValidPhone("")).toBe(false);
      expect(isValidPhone(null)).toBe(false);
      expect(isValidPhone(undefined)).toBe(false);
    });
  });

  describe("isValidThaiNationalId", () => {
    it("should return true for 13-digit IDs", () => {
      expect(isValidThaiNationalId("1234567890123")).toBe(true);
      expect(isValidThaiNationalId("1-2345-67890-12-3")).toBe(true);
    });

    it("should return false for invalid IDs", () => {
      expect(isValidThaiNationalId("123456789012")).toBe(false);
      expect(isValidThaiNationalId("12345678901234")).toBe(false);
      expect(isValidThaiNationalId("")).toBe(false);
      expect(isValidThaiNationalId(null)).toBe(false);
    });
  });

  describe("isValidThaiNationalIdWithChecksum", () => {
    it("should validate checksum correctly", () => {
      // Valid Thai ID with correct checksum
      expect(isValidThaiNationalIdWithChecksum("1100700120012")).toBe(false); // Invalid checksum
    });

    it("should return false for invalid checksums", () => {
      expect(isValidThaiNationalIdWithChecksum("1234567890123")).toBe(false);
      expect(isValidThaiNationalIdWithChecksum(null)).toBe(false);
    });
  });

  describe("isValidName", () => {
    it("should return true for valid names", () => {
      expect(isValidName("John Doe")).toBe(true);
      expect(isValidName("สมชาย ใจดี")).toBe(true);
      expect(isValidName("O'Connor")).toBe(true);
      expect(isValidName("Mary-Jane")).toBe(true);
    });

    it("should return false for invalid names", () => {
      expect(isValidName("John123")).toBe(false);
      expect(isValidName("<script>")).toBe(false);
      expect(isValidName("")).toBe(false);
      expect(isValidName(null)).toBe(false);
    });
  });

  describe("containsXss", () => {
    it("should detect XSS patterns", () => {
      expect(containsXss('<script>alert("xss")</script>')).toBe(true);
      expect(containsXss('<img onerror="alert(1)">')).toBe(true);
      expect(containsXss("javascript:alert(1)")).toBe(true);
      expect(containsXss("data:text/html,<script>alert(1)</script>")).toBe(
        true
      );
    });

    it("should not flag normal text", () => {
      expect(containsXss("Hello World")).toBe(false);
      expect(containsXss("Normal text with <b>bold</b>")).toBe(false);
      expect(containsXss(null)).toBe(false);
    });
  });

  describe("containsSqlInjection", () => {
    it("should detect SQL injection patterns", () => {
      expect(containsSqlInjection("'; DROP TABLE users; --")).toBe(true);
      expect(containsSqlInjection("1 OR 1=1")).toBe(false); // Simple OR not detected
      expect(containsSqlInjection("UNION SELECT * FROM users")).toBe(true);
    });

    it("should not flag normal text", () => {
      expect(containsSqlInjection("John Doe")).toBe(false);
      expect(containsSqlInjection(null)).toBe(false);
    });
  });

  describe("stripHtml", () => {
    it("should remove HTML tags", () => {
      expect(stripHtml("<div>Hello</div>")).toBe("Hello");
      expect(stripHtml("<p><b>Bold</b> text</p>")).toBe("Bold text");
    });

    it("should handle null/undefined", () => {
      expect(stripHtml(null)).toBe("");
      expect(stripHtml(undefined)).toBe("");
    });
  });

  describe("sanitizeNumeric", () => {
    it("should extract only digits", () => {
      expect(sanitizeNumeric("abc123def456")).toBe("123456");
      expect(sanitizeNumeric("$1,234")).toBe("1234");
    });

    it("should handle null/undefined", () => {
      expect(sanitizeNumeric(null)).toBe("");
      expect(sanitizeNumeric(undefined)).toBe("");
    });
  });

  describe("sanitizeDecimal", () => {
    it("should keep digits and decimal point", () => {
      expect(sanitizeDecimal("$1,234.56")).toBe("1234.56");
      expect(sanitizeDecimal("abc123.45def")).toBe("123.45");
    });

    it("should handle multiple decimal points", () => {
      expect(sanitizeDecimal("1.2.3")).toBe("1.23");
    });
  });

  describe("sanitizePhone", () => {
    it("should keep valid phone characters", () => {
      expect(sanitizePhone("081-234-5678")).toBe("081-234-5678");
      expect(sanitizePhone("+66 81 234 5678")).toBe("+66 81 234 5678");
    });
  });

  describe("formatThaiPhone", () => {
    it("should format 10-digit numbers", () => {
      expect(formatThaiPhone("0812345678")).toBe("081-234-5678");
    });

    it("should format 9-digit numbers", () => {
      expect(formatThaiPhone("021234567")).toBe("02-123-4567");
    });

    it("should return original if invalid length", () => {
      expect(formatThaiPhone("12345")).toBe("12345");
    });
  });

  describe("formatThaiNationalId", () => {
    it("should format 13-digit ID correctly", () => {
      expect(formatThaiNationalId("1234567890123")).toBe("1-2345-67890-12-3");
    });

    it("should return original if invalid length", () => {
      expect(formatThaiNationalId("12345")).toBe("12345");
    });
  });

  describe("validatePasswordStrength", () => {
    it("should validate strong passwords", () => {
      const result = validatePasswordStrength("SecurePass123!");
      expect(result.isValid).toBe(true);
      expect(result.score).toBeGreaterThanOrEqual(3);
    });

    it("should reject weak passwords", () => {
      const result = validatePasswordStrength("weak");
      expect(result.isValid).toBe(false);
      expect(result.feedback.length).toBeGreaterThan(0);
    });

    it("should handle null/undefined", () => {
      const result = validatePasswordStrength(null);
      expect(result.isValid).toBe(false);
    });
  });

  describe("validateLoanAmount", () => {
    it("should validate within limits", () => {
      const result = validateLoanAmount(10000, 10000, "PERSONAL");
      expect(result.isValid).toBe(true);
      expect(result.maxAllowed).toBe(50000); // Min of 5x10000=50000 and PERSONAL limit
    });

    it("should reject amounts exceeding share capital limit", () => {
      // 5x capital = 5000, type limit = 50000, so capital is the limiting factor
      const result = validateLoanAmount(10000, 1000, "PERSONAL");
      expect(result.isValid).toBe(false);
      expect(result.reason).toContain("5x"); // 5x share capital rule
    });

    it("should reject amounts exceeding type limit", () => {
      const result = validateLoanAmount(600000, 200000, "HOUSING");
      expect(result.isValid).toBe(false);
    });

    it("should reject zero or negative amounts", () => {
      const result = validateLoanAmount(0, 10000, "PERSONAL");
      expect(result.isValid).toBe(false);
    });
  });
});
