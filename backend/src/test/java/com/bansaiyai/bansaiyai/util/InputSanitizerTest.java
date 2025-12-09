package com.bansaiyai.bansaiyai.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InputSanitizer utility class.
 */
@DisplayName("InputSanitizer Tests")
class InputSanitizerTest {

  private InputSanitizer sanitizer;

  @BeforeEach
  void setUp() {
    sanitizer = new InputSanitizer();
  }

  @Nested
  @DisplayName("sanitizeText tests")
  class SanitizeTextTests {

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNull() {
      assertThat(sanitizer.sanitizeText(null)).isNull();
    }

    @Test
    @DisplayName("should encode HTML entities")
    void shouldEncodeHtmlEntities() {
      String input = "<script>alert('xss')</script>";
      String result = sanitizer.sanitizeText(input);
      assertThat(result).doesNotContain("<script>").doesNotContain("</script>");
    }

    @Test
    @DisplayName("should trim whitespace")
    void shouldTrimWhitespace() {
      String input = "  test string  ";
      String result = sanitizer.sanitizeText(input);
      assertThat(result).isEqualTo("test string");
    }
  }

  @Nested
  @DisplayName("XSS detection tests")
  class XssDetectionTests {

    @Test
    @DisplayName("should detect script tags")
    void shouldDetectScriptTags() {
      assertThat(sanitizer.containsXss("<script>alert('xss')</script>")).isTrue();
    }

    @Test
    @DisplayName("should detect event handlers")
    void shouldDetectEventHandlers() {
      assertThat(sanitizer.containsXss("<img onerror=\"alert('xss')\">")).isTrue();
    }

    @Test
    @DisplayName("should detect javascript protocol")
    void shouldDetectJavascriptProtocol() {
      assertThat(sanitizer.containsXss("javascript:alert('xss')")).isTrue();
    }

    @Test
    @DisplayName("should not flag normal text")
    void shouldNotFlagNormalText() {
      assertThat(sanitizer.containsXss("Hello World")).isFalse();
    }

    @Test
    @DisplayName("should return false for null")
    void shouldReturnFalseForNull() {
      assertThat(sanitizer.containsXss(null)).isFalse();
    }
  }

  @Nested
  @DisplayName("SQL injection detection tests")
  class SqlInjectionDetectionTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "'; DROP TABLE users; --",
        "1 OR 1=1",
        "UNION SELECT * FROM users",
        "'; exec xp_cmdshell('dir'); --"
    })
    @DisplayName("should detect SQL injection patterns")
    void shouldDetectSqlInjection(String input) {
      assertThat(sanitizer.containsSqlInjection(input)).isTrue();
    }

    @Test
    @DisplayName("should not flag normal text")
    void shouldNotFlagNormalText() {
      assertThat(sanitizer.containsSqlInjection("John Doe")).isFalse();
    }
  }

  @Nested
  @DisplayName("Email validation tests")
  class EmailValidationTests {

    @ParameterizedTest
    @CsvSource({
        "test@example.com, true",
        "user.name@domain.co.th, true",
        "invalid-email, false",
        "@missing-local.com, false",
        "missing-domain@.com, false"
    })
    @DisplayName("should validate email format")
    void shouldValidateEmail(String email, boolean expected) {
      assertThat(sanitizer.isValidEmail(email)).isEqualTo(expected);
    }

    @Test
    @DisplayName("should return false for null")
    void shouldReturnFalseForNull() {
      assertThat(sanitizer.isValidEmail(null)).isFalse();
    }
  }

  @Nested
  @DisplayName("Phone validation tests")
  class PhoneValidationTests {

    @ParameterizedTest
    @ValueSource(strings = { "0812345678", "081-234-5678", "+66812345678" })
    @DisplayName("should validate Thai phone numbers")
    void shouldValidateThaiPhoneNumbers(String phone) {
      assertThat(sanitizer.isValidPhone(phone)).isTrue();
    }

    @Test
    @DisplayName("should reject invalid phone numbers")
    void shouldRejectInvalidPhoneNumbers() {
      assertThat(sanitizer.isValidPhone("123")).isFalse();
    }
  }

  @Nested
  @DisplayName("Thai National ID validation tests")
  class ThaiNationalIdValidationTests {

    @Test
    @DisplayName("should validate 13-digit ID")
    void shouldValidate13DigitId() {
      assertThat(sanitizer.isValidThaiNationalId("1234567890123")).isTrue();
    }

    @Test
    @DisplayName("should reject invalid length")
    void shouldRejectInvalidLength() {
      assertThat(sanitizer.isValidThaiNationalId("123456789")).isFalse();
    }

    @Test
    @DisplayName("should reject non-numeric ID")
    void shouldRejectNonNumericId() {
      assertThat(sanitizer.isValidThaiNationalId("123456789012a")).isFalse();
    }
  }

  @Nested
  @DisplayName("Name sanitization tests")
  class NameSanitizationTests {

    @Test
    @DisplayName("should accept Thai names")
    void shouldAcceptThaiNames() {
      String result = sanitizer.sanitizeName("สมชาย ใจดี");
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should accept English names")
    void shouldAcceptEnglishNames() {
      String result = sanitizer.sanitizeName("John Doe");
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should reject names with numbers")
    void shouldRejectNamesWithNumbers() {
      String result = sanitizer.sanitizeName("John123");
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should reject names with special characters")
    void shouldRejectNamesWithSpecialChars() {
      String result = sanitizer.sanitizeName("John<script>");
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("Numeric sanitization tests")
  class NumericSanitizationTests {

    @Test
    @DisplayName("should extract only digits")
    void shouldExtractOnlyDigits() {
      assertThat(sanitizer.sanitizeNumeric("abc123def456")).isEqualTo("123456");
    }

    @Test
    @DisplayName("should handle decimal numbers")
    void shouldHandleDecimalNumbers() {
      assertThat(sanitizer.sanitizeDecimal("$1,234.56")).isEqualTo("1234.56");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should handle null and empty input")
    void shouldHandleNullAndEmpty(String input) {
      if (input == null) {
        assertThat(sanitizer.sanitizeNumeric(input)).isNull();
      }
    }
  }

  @Nested
  @DisplayName("HTML stripping tests")
  class HtmlStrippingTests {

    @Test
    @DisplayName("should strip all HTML tags")
    void shouldStripAllHtmlTags() {
      String input = "<div><p>Hello</p><script>alert('xss')</script></div>";
      String result = sanitizer.stripHtml(input);
      assertThat(result).isEqualTo("Helloalert('xss')");
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNull() {
      assertThat(sanitizer.stripHtml(null)).isNull();
    }
  }
}
