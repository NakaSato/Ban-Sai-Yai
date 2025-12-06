import * as yup from "yup";
import {
  isValidEmail,
  isValidPhone,
  isValidThaiNationalId,
  isValidThaiNationalIdWithChecksum,
  isValidName,
  containsXss,
  validatePasswordStrength,
} from "@/utils/validation";

/**
 * Custom Yup validation methods for Thai savings group
 */

// Add custom test methods to Yup
yup.addMethod(
  yup.string,
  "thaiPhone",
  function (message = "Invalid Thai phone number") {
    return this.test("thai-phone", message, (value) => {
      if (!value) return true; // Allow empty - use required() for required fields
      return isValidPhone(value);
    });
  }
);

yup.addMethod(
  yup.string,
  "thaiNationalId",
  function (message = "Invalid Thai national ID") {
    return this.test("thai-national-id", message, (value) => {
      if (!value) return true;
      return isValidThaiNationalId(value);
    });
  }
);

yup.addMethod(
  yup.string,
  "thaiNationalIdWithChecksum",
  function (message = "Invalid Thai national ID") {
    return this.test("thai-national-id-checksum", message, (value) => {
      if (!value) return true;
      return isValidThaiNationalIdWithChecksum(value);
    });
  }
);

yup.addMethod(
  yup.string,
  "noXss",
  function (message = "Input contains invalid characters") {
    return this.test("no-xss", message, (value) => {
      if (!value) return true;
      return !containsXss(value);
    });
  }
);

yup.addMethod(yup.string, "strongPassword", function () {
  return this.test("strong-password", "", function (value) {
    if (!value) return true;
    const result = validatePasswordStrength(value);
    if (!result.isValid && result.feedback.length > 0) {
      return this.createError({ message: result.feedback[0] });
    }
    return true;
  });
});

// Type declarations for custom methods
declare module "yup" {
  interface StringSchema {
    thaiPhone(message?: string): StringSchema;
    thaiNationalId(message?: string): StringSchema;
    thaiNationalIdWithChecksum(message?: string): StringSchema;
    noXss(message?: string): StringSchema;
    strongPassword(): StringSchema;
  }
}

/**
 * Login form schema
 */
export const loginSchema = yup.object({
  username: yup
    .string()
    .required("Username is required")
    .min(3, "Username must be at least 3 characters")
    .max(50, "Username must be at most 50 characters"),
  password: yup
    .string()
    .required("Password is required")
    .min(6, "Password must be at least 6 characters"),
  rememberMe: yup.boolean().optional(),
});

export type LoginFormData = yup.InferType<typeof loginSchema>;

/**
 * Registration form schema
 */
export const registerSchema = yup.object({
  username: yup
    .string()
    .required("Username is required")
    .min(3, "Username must be at least 3 characters")
    .max(50, "Username must be at most 50 characters")
    .matches(
      /^[a-zA-Z0-9_]+$/,
      "Username can only contain letters, numbers, and underscores"
    ),
  email: yup
    .string()
    .required("Email is required")
    .email("Invalid email address"),
  password: yup
    .string()
    .required("Password is required")
    .min(8, "Password must be at least 8 characters")
    .strongPassword(),
  confirmPassword: yup
    .string()
    .required("Please confirm your password")
    .oneOf([yup.ref("password")], "Passwords must match"),
  firstName: yup
    .string()
    .required("First name is required")
    .min(2, "First name must be at least 2 characters")
    .noXss(),
  lastName: yup
    .string()
    .required("Last name is required")
    .min(2, "Last name must be at least 2 characters")
    .noXss(),
  phone: yup.string().optional().thaiPhone(),
  address: yup
    .string()
    .optional()
    .max(500, "Address must be at most 500 characters")
    .noXss(),
  dateOfBirth: yup.string().optional(),
});

export type RegisterFormData = yup.InferType<typeof registerSchema>;

/**
 * Forgot password form schema
 */
export const forgotPasswordSchema = yup.object({
  email: yup
    .string()
    .required("Email is required")
    .email("Invalid email address"),
});

export type ForgotPasswordFormData = yup.InferType<typeof forgotPasswordSchema>;

/**
 * Reset password form schema
 */
export const resetPasswordSchema = yup.object({
  token: yup.string().required("Reset token is required"),
  newPassword: yup
    .string()
    .required("New password is required")
    .min(8, "Password must be at least 8 characters")
    .strongPassword(),
  confirmPassword: yup
    .string()
    .required("Please confirm your password")
    .oneOf([yup.ref("newPassword")], "Passwords must match"),
});

export type ResetPasswordFormData = yup.InferType<typeof resetPasswordSchema>;

/**
 * Member form schema
 */
export const memberSchema = yup.object({
  firstName: yup
    .string()
    .required("First name is required")
    .min(2, "First name must be at least 2 characters")
    .max(100, "First name must be at most 100 characters")
    .noXss(),
  lastName: yup
    .string()
    .required("Last name is required")
    .min(2, "Last name must be at least 2 characters")
    .max(100, "Last name must be at most 100 characters")
    .noXss(),
  email: yup
    .string()
    .required("Email is required")
    .email("Invalid email address"),
  phone: yup.string().required("Phone number is required").thaiPhone(),
  nationalId: yup.string().optional().thaiNationalIdWithChecksum(),
  address: yup
    .string()
    .required("Address is required")
    .max(500, "Address must be at most 500 characters")
    .noXss(),
  dateOfBirth: yup.string().required("Date of birth is required"),
});

export type MemberFormData = yup.InferType<typeof memberSchema>;

/**
 * Loan application form schema
 */
export const loanApplicationSchema = yup.object({
  memberId: yup.string().required("Member is required"),
  type: yup
    .string()
    .required("Loan type is required")
    .oneOf(
      ["PERSONAL", "BUSINESS", "EMERGENCY", "EDUCATION", "HOUSING"],
      "Invalid loan type"
    ),
  amount: yup
    .number()
    .required("Loan amount is required")
    .min(1000, "Minimum loan amount is ฿1,000")
    .max(500000, "Maximum loan amount is ฿500,000"),
  term: yup
    .number()
    .required("Loan term is required")
    .min(1, "Minimum term is 1 month")
    .max(60, "Maximum term is 60 months"),
  purpose: yup
    .string()
    .required("Purpose is required")
    .min(10, "Purpose must be at least 10 characters")
    .max(500, "Purpose must be at most 500 characters")
    .noXss(),
});

export type LoanApplicationFormData = yup.InferType<
  typeof loanApplicationSchema
>;

/**
 * Payment form schema
 */
export const paymentSchema = yup.object({
  loanId: yup.string().required("Loan is required"),
  amount: yup
    .number()
    .required("Payment amount is required")
    .min(1, "Payment amount must be greater than 0"),
  paymentMethod: yup
    .string()
    .required("Payment method is required")
    .oneOf(["CASH", "BANK_TRANSFER", "CHECK"], "Invalid payment method"),
  notes: yup
    .string()
    .optional()
    .max(500, "Notes must be at most 500 characters")
    .noXss(),
});

export type PaymentFormData = yup.InferType<typeof paymentSchema>;

/**
 * Deposit form schema
 */
export const depositSchema = yup.object({
  memberId: yup.string().required("Member is required"),
  accountId: yup.string().required("Account is required"),
  amount: yup
    .number()
    .required("Deposit amount is required")
    .min(1, "Deposit amount must be greater than 0"),
  notes: yup
    .string()
    .optional()
    .max(500, "Notes must be at most 500 characters")
    .noXss(),
});

export type DepositFormData = yup.InferType<typeof depositSchema>;

/**
 * Withdrawal form schema
 */
export const withdrawalSchema = yup.object({
  memberId: yup.string().required("Member is required"),
  accountId: yup.string().required("Account is required"),
  amount: yup
    .number()
    .required("Withdrawal amount is required")
    .min(1, "Withdrawal amount must be greater than 0"),
  notes: yup
    .string()
    .optional()
    .max(500, "Notes must be at most 500 characters")
    .noXss(),
});

export type WithdrawalFormData = yup.InferType<typeof withdrawalSchema>;

/**
 * Collateral form schema
 */
export const collateralSchema = yup.object({
  type: yup
    .string()
    .required("Collateral type is required")
    .oneOf(
      ["REAL_ESTATE", "VEHICLE", "JEWELRY", "EQUIPMENT", "OTHER"],
      "Invalid collateral type"
    ),
  description: yup
    .string()
    .required("Description is required")
    .min(10, "Description must be at least 10 characters")
    .max(500, "Description must be at most 500 characters")
    .noXss(),
  value: yup
    .number()
    .required("Collateral value is required")
    .min(1, "Collateral value must be greater than 0"),
});

export type CollateralFormData = yup.InferType<typeof collateralSchema>;

/**
 * Guarantor form schema
 */
export const guarantorSchema = yup.object({
  name: yup
    .string()
    .required("Guarantor name is required")
    .min(2, "Name must be at least 2 characters")
    .noXss(),
  relationship: yup.string().required("Relationship is required").noXss(),
  phone: yup.string().required("Phone number is required").thaiPhone(),
  email: yup.string().optional().email("Invalid email address"),
  address: yup
    .string()
    .required("Address is required")
    .max(500, "Address must be at most 500 characters")
    .noXss(),
  idDocument: yup.string().required("ID document is required").noXss(),
});

export type GuarantorFormData = yup.InferType<typeof guarantorSchema>;

/**
 * Profile update form schema
 */
export const profileUpdateSchema = yup.object({
  firstName: yup
    .string()
    .required("First name is required")
    .min(2, "First name must be at least 2 characters")
    .noXss(),
  lastName: yup
    .string()
    .required("Last name is required")
    .min(2, "Last name must be at least 2 characters")
    .noXss(),
  email: yup
    .string()
    .required("Email is required")
    .email("Invalid email address"),
  phone: yup.string().optional().thaiPhone(),
});

export type ProfileUpdateFormData = yup.InferType<typeof profileUpdateSchema>;

/**
 * Change password form schema
 */
export const changePasswordSchema = yup.object({
  currentPassword: yup.string().required("Current password is required"),
  newPassword: yup
    .string()
    .required("New password is required")
    .min(8, "Password must be at least 8 characters")
    .strongPassword(),
  confirmPassword: yup
    .string()
    .required("Please confirm your password")
    .oneOf([yup.ref("newPassword")], "Passwords must match"),
});

export type ChangePasswordFormData = yup.InferType<typeof changePasswordSchema>;
