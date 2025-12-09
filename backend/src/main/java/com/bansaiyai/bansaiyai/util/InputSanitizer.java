package com.bansaiyai.bansaiyai.util;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for input sanitization to prevent XSS and injection attacks.
 * Uses OWASP Encoder for proper encoding.
 */
@Component
public class InputSanitizer {

  // Patterns for validation
  private static final Pattern SCRIPT_PATTERN = Pattern.compile(
      "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
      "on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
  private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
      "(--|;|'|\"|\\/\\*|\\*\\/|xp_|exec|execute|insert|select|delete|update|drop|create|alter|union|into|load_file|outfile|\\s+or\\s+)",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile(
      "^[0-9+\\-()\\s]{8,20}$");
  private static final Pattern THAI_ID_PATTERN = Pattern.compile(
      "^[0-9]{13}$");

  /**
   * Sanitize general text input by encoding HTML entities.
   *
   * @param input the input string
   * @return sanitized string
   */
  public String sanitizeText(String input) {
    if (input == null) {
      return null;
    }
    return Encode.forHtml(input.trim());
  }

  /**
   * Sanitize text for HTML context.
   *
   * @param input the input string
   * @return HTML-encoded string
   */
  public String sanitizeForHtml(String input) {
    if (input == null) {
      return null;
    }
    return Encode.forHtml(input);
  }

  /**
   * Sanitize text for JavaScript context.
   *
   * @param input the input string
   * @return JavaScript-encoded string
   */
  public String sanitizeForJavaScript(String input) {
    if (input == null) {
      return null;
    }
    return Encode.forJavaScript(input);
  }

  /**
   * Sanitize text for CSS context.
   *
   * @param input the input string
   * @return CSS-encoded string
   */
  public String sanitizeForCss(String input) {
    if (input == null) {
      return null;
    }
    return Encode.forCssString(input);
  }

  /**
   * Sanitize text for URL parameter.
   *
   * @param input the input string
   * @return URL-encoded string
   */
  public String sanitizeForUrl(String input) {
    if (input == null) {
      return null;
    }
    return Encode.forUriComponent(input);
  }

  /**
   * Strip all HTML tags from input.
   *
   * @param input the input string
   * @return string with HTML tags removed
   */
  public String stripHtml(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("<[^>]*>", "").trim();
  }

  /**
   * Check if input contains potential XSS attack patterns.
   *
   * @param input the input string
   * @return true if XSS patterns are detected
   */
  public boolean containsXss(String input) {
    if (input == null) {
      return false;
    }
    String lower = input.toLowerCase();
    return SCRIPT_PATTERN.matcher(lower).find()
        || EVENT_HANDLER_PATTERN.matcher(lower).find()
        || lower.contains("javascript:")
        || lower.contains("data:")
        || lower.contains("vbscript:");
  }

  /**
   * Check if input contains potential SQL injection patterns.
   *
   * @param input the input string
   * @return true if SQL injection patterns are detected
   */
  public boolean containsSqlInjection(String input) {
    if (input == null) {
      return false;
    }
    return SQL_INJECTION_PATTERN.matcher(input).find();
  }

  /**
   * Validate email format.
   *
   * @param email the email string
   * @return true if valid email format
   */
  public boolean isValidEmail(String email) {
    if (email == null) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Validate Thai phone number format.
   *
   * @param phone the phone string
   * @return true if valid phone format
   */
  public boolean isValidPhone(String phone) {
    if (phone == null) {
      return false;
    }
    String digits = phone.replaceAll("[^0-9]", "");
    return digits.length() >= 9 && digits.length() <= 15 && PHONE_PATTERN.matcher(phone).matches();
  }

  /**
   * Validate Thai national ID format.
   *
   * @param nationalId the national ID string
   * @return true if valid 13-digit format
   */
  public boolean isValidThaiNationalId(String nationalId) {
    if (nationalId == null) {
      return false;
    }
    String digits = nationalId.replaceAll("[^0-9]", "");
    return THAI_ID_PATTERN.matcher(digits).matches();
  }

  /**
   * Sanitize and validate name (Thai or English).
   * Allows letters, spaces, and common Thai characters.
   *
   * @param name the name string
   * @return sanitized name or null if invalid
   */
  public String sanitizeName(String name) {
    if (name == null) {
      return null;
    }
    String trimmed = name.trim();
    // Allow Thai, English letters, spaces, and common punctuation
    if (!trimmed.matches("^[\\p{L}\\p{M}\\s.'-]+$")) {
      return null;
    }
    return sanitizeText(trimmed);
  }

  /**
   * Sanitize numeric input - keep only digits.
   *
   * @param input the input string
   * @return string with only digits
   */
  public String sanitizeNumeric(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("[^0-9]", "");
  }

  /**
   * Sanitize decimal input - keep digits and decimal point.
   *
   * @param input the input string
   * @return string with only digits and one decimal point
   */
  public String sanitizeDecimal(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("[^0-9.]", "");
  }
}
