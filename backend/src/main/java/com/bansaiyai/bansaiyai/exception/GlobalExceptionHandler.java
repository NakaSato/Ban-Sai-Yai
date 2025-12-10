package com.bansaiyai.bansaiyai.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Provides consistent error responses using RFC 7807 ProblemDetail.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handle validation errors from @Valid annotations
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Validation failed: {}", errors);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid input data");
    problemDetail.setTitle("Validation Failed");
    problemDetail.setProperty("timestamp", LocalDateTime.now());
    problemDetail.setProperty("validationErrors", errors);

    return ResponseEntity.badRequest().body(problemDetail);
  }

  /**
   * Handle constraint violations from @Validated
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation -> {
      String fieldName = violation.getPropertyPath().toString();
      String errorMessage = violation.getMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Constraint violation: {}", errors);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid input data");
    problemDetail.setTitle("Constraint Violation");
    problemDetail.setProperty("timestamp", LocalDateTime.now());
    problemDetail.setProperty("validationErrors", errors);

    return ResponseEntity.badRequest().body(problemDetail);
  }

  /**
   * Handle entity not found exceptions
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleEntityNotFound(
      EntityNotFoundException ex, WebRequest request) {

    log.warn("Entity not found: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Not Found");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
  }

  /**
   * Handle resource not found (custom exception)
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleResourceNotFound(
      ResourceNotFoundException ex, WebRequest request) {

    log.warn("Resource not found: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Resource Not Found");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
  }

  /**
   * Handle business logic exceptions
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ProblemDetail> handleBusinessException(
      BusinessException ex, WebRequest request) {

    log.warn("Business error: {}", ex.getMessage());

    HttpStatus status = ex.getHttpStatus();
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    problemDetail.setTitle("Business Rule Violation");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(status).body(problemDetail);
  }

  /**
   * Handle authentication failures
   */
  @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
  public ResponseEntity<ProblemDetail> handleAuthenticationException(
      Exception ex, WebRequest request) {

    log.warn("Authentication failed: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
        "Invalid username or password");
    problemDetail.setTitle("Authentication Failed");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  /**
   * Handle access denied exceptions
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {

    log.warn("Access denied: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
        "You do not have permission to access this resource");
    problemDetail.setTitle("Access Denied");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
  }

  /**
   * Handle data integrity violations (duplicate keys, foreign key constraints)
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, WebRequest request) {

    log.error("Data integrity violation: {}", ex.getMessage());

    String message = "Data integrity violation";
    if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
      message = "A record with this information already exists";
    }

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    problemDetail.setTitle("Data Conflict");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
  }

  /**
   * Handle all other exceptions
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleAllExceptions(
      Exception ex, WebRequest request) {

    log.error("Unexpected error occurred", ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred. Please try again later.");
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
  }
}
