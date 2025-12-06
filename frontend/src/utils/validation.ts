/**
 * Input validation utilities for Thai savings group application.
 * Matches backend InputSanitizer patterns.
 */

// Validation patterns
const EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const PHONE_PATTERN = /^[0-9+\-() ]{8,20}$/;
const THAI_ID_PATTERN = /^[0-9]{13}$/;
// Thai name pattern: letters, marks (for Thai vowels/tones), spaces, and common punctuation
const THAI_NAME_PATTERN = /^[\p{L}\p{M}\s.''-]+$/u;
const NUMERIC_PATTERN = /^[0-9]+$/;
const DECIMAL_PATTERN = /^[0-9]+(\.[0-9]+)?$/;

// XSS patterns to detect
const XSS_PATTERNS = [
  /<script[^>]*>.*?<\/script>/gi,
  /on\w+\s*=/gi,
  /javascript:/gi,
  /data:/gi,
  /vbscript:/gi,
];

// SQL injection patterns
const SQL_INJECTION_PATTERN =
  /(--|;|'|"|\/\*|\*\/|xp_|exec|execute|insert|select|delete|update|drop|create|alter|union|into|load_file|outfile)/i;

/**
 * Validate email format
 */
export function isValidEmail(email: string | null | undefined): boolean {
  if (!email) return false;
  return EMAIL_PATTERN.test(email);
}

/**
 * Validate Thai phone number format
 * Supports: 0812345678, 081-234-5678, +66812345678, 02-123-4567
 */
export function isValidPhone(phone: string | null | undefined): boolean {
  if (!phone) return false;
  const digits = phone.replace(/[^0-9]/g, "");
  // Thai phone: 9-10 digits (local) or 11 digits (international +66)
  return digits.length >= 9 && digits.length <= 11 && PHONE_PATTERN.test(phone);
}

/**
 * Validate Thai national ID (13 digits)
 */
export function isValidThaiNationalId(
  nationalId: string | null | undefined
): boolean {
  if (!nationalId) return false;
  const digits = nationalId.replace(/[^0-9]/g, "");
  return THAI_ID_PATTERN.test(digits);
}

/**
 * Validate Thai national ID with checksum
 */
export function isValidThaiNationalIdWithChecksum(
  nationalId: string | null | undefined
): boolean {
  if (!isValidThaiNationalId(nationalId)) return false;

  const digits = nationalId!.replace(/[^0-9]/g, "");
  let sum = 0;
  for (let i = 0; i < 12; i++) {
    sum += parseInt(digits[i]) * (13 - i);
  }
  const checkDigit = (11 - (sum % 11)) % 10;
  return checkDigit === parseInt(digits[12]);
}

/**
 * Validate name (Thai or English)
 */
export function isValidName(name: string | null | undefined): boolean {
  if (!name) return false;
  return THAI_NAME_PATTERN.test(name.trim());
}

/**
 * Check if input contains potential XSS patterns
 */
export function containsXss(input: string | null | undefined): boolean {
  if (!input) return false;
  return XSS_PATTERNS.some((pattern) => pattern.test(input));
}

/**
 * Check if input contains potential SQL injection patterns
 */
export function containsSqlInjection(
  input: string | null | undefined
): boolean {
  if (!input) return false;
  return SQL_INJECTION_PATTERN.test(input);
}

/**
 * Sanitize text by removing HTML tags
 */
export function stripHtml(input: string | null | undefined): string {
  if (!input) return "";
  return input.replace(/<[^>]*>/g, "").trim();
}

/**
 * Sanitize numeric input - keep only digits
 */
export function sanitizeNumeric(input: string | null | undefined): string {
  if (!input) return "";
  return input.replace(/[^0-9]/g, "");
}

/**
 * Sanitize decimal input - keep digits and decimal point
 */
export function sanitizeDecimal(input: string | null | undefined): string {
  if (!input) return "";
  // Keep only first decimal point
  const parts = input.replace(/[^0-9.]/g, "").split(".");
  return parts.length > 1 ? `${parts[0]}.${parts.slice(1).join("")}` : parts[0];
}

/**
 * Sanitize phone number - format as Thai phone
 */
export function sanitizePhone(input: string | null | undefined): string {
  if (!input) return "";
  return input.replace(/[^0-9+\-() ]/g, "");
}

/**
 * Format Thai phone number for display
 */
export function formatThaiPhone(phone: string | null | undefined): string {
  if (!phone) return "";
  const digits = phone.replace(/[^0-9]/g, "");
  if (digits.length === 10) {
    return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  }
  if (digits.length === 9) {
    return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`;
  }
  return phone;
}

/**
 * Format Thai national ID for display
 */
export function formatThaiNationalId(id: string | null | undefined): string {
  if (!id) return "";
  const digits = id.replace(/[^0-9]/g, "");
  if (digits.length !== 13) return id;
  return `${digits[0]}-${digits.slice(1, 5)}-${digits.slice(
    5,
    10
  )}-${digits.slice(10, 12)}-${digits[12]}`;
}

/**
 * Validate password strength
 */
export interface PasswordStrength {
  isValid: boolean;
  score: number; // 0-4
  feedback: string[];
}

export function validatePasswordStrength(
  password: string | null | undefined
): PasswordStrength {
  if (!password) {
    return { isValid: false, score: 0, feedback: ["Password is required"] };
  }

  const feedback: string[] = [];
  let score = 0;

  // Length check
  if (password.length < 8) {
    feedback.push("Password must be at least 8 characters");
  } else {
    score++;
    if (password.length >= 12) score++;
  }

  // Uppercase check
  if (!/[A-Z]/.test(password)) {
    feedback.push("Include at least one uppercase letter");
  } else {
    score++;
  }

  // Lowercase check
  if (!/[a-z]/.test(password)) {
    feedback.push("Include at least one lowercase letter");
  }

  // Number check
  if (!/[0-9]/.test(password)) {
    feedback.push("Include at least one number");
  } else {
    score++;
  }

  // Special character check
  if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    feedback.push("Include at least one special character");
  } else {
    score++;
  }

  return {
    isValid: feedback.length === 0 && password.length >= 8,
    score: Math.min(score, 4),
    feedback,
  };
}

/**
 * Validate loan amount against Thai savings group rules
 */
export interface LoanAmountValidation {
  isValid: boolean;
  maxAllowed: number;
  reason?: string;
}

export function validateLoanAmount(
  requestedAmount: number,
  shareCapital: number,
  loanType: "PERSONAL" | "BUSINESS" | "EMERGENCY" | "EDUCATION" | "HOUSING"
): LoanAmountValidation {
  // Loan type limits (in THB)
  const LOAN_LIMITS: Record<string, number> = {
    PERSONAL: 50000,
    BUSINESS: 200000,
    EMERGENCY: 20000,
    EDUCATION: 100000,
    HOUSING: 500000,
  };

  // Max is 5x share capital or type limit, whichever is lower
  const maxByCapital = shareCapital * 5;
  const maxByType = LOAN_LIMITS[loanType] || 50000;
  const maxAllowed = Math.min(maxByCapital, maxByType);

  if (requestedAmount <= 0) {
    return {
      isValid: false,
      maxAllowed,
      reason: "Amount must be greater than 0",
    };
  }

  if (requestedAmount > maxAllowed) {
    if (maxByCapital < maxByType) {
      return {
        isValid: false,
        maxAllowed,
        reason: `Maximum loan amount is 5x your share capital (฿${maxAllowed.toLocaleString()})`,
      };
    }
    return {
      isValid: false,
      maxAllowed,
      reason: `Maximum ${loanType.toLowerCase()} loan is ฿${maxByType.toLocaleString()}`,
    };
  }

  return { isValid: true, maxAllowed };
}
