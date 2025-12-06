package com.bansaiyai.bansaiyai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown for business rule violations.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

  private final String errorCode;
  private final HttpStatus httpStatus;

  public BusinessException(String message) {
    super(message);
    this.errorCode = "BUSINESS_ERROR";
    this.httpStatus = HttpStatus.BAD_REQUEST;
  }

  public BusinessException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = HttpStatus.BAD_REQUEST;
  }

  public BusinessException(String message, String errorCode, HttpStatus httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = "BUSINESS_ERROR";
    this.httpStatus = HttpStatus.BAD_REQUEST;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}
