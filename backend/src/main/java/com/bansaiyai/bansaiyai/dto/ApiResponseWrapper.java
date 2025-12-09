package com.bansaiyai.bansaiyai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for consistent response format.
 * All API endpoints should return responses wrapped in this structure.
 *
 * @param <T> the type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseWrapper<T> {

  /**
   * Indicates if the request was successful.
   */
  private boolean success;

  /**
   * Human-readable message about the response.
   */
  private String message;

  /**
   * The actual response data.
   */
  private T data;

  /**
   * Timestamp of the response.
   */
  @Builder.Default
  private LocalDateTime timestamp = LocalDateTime.now();

  /**
   * Request tracking ID for debugging.
   */
  private String requestId;

  /**
   * Error details if the request failed.
   */
  private ErrorDetails error;

  /**
   * Pagination information if applicable.
   */
  private PageInfo pagination;

  /**
   * Create a successful response with data.
   */
  public static <T> ApiResponseWrapper<T> success(T data) {
    return ApiResponseWrapper.<T>builder()
        .success(true)
        .message("Success")
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create a successful response with data and custom message.
   */
  public static <T> ApiResponseWrapper<T> success(T data, String message) {
    return ApiResponseWrapper.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create a successful response with pagination info.
   */
  public static <T> ApiResponseWrapper<T> success(T data, PageInfo pagination) {
    return ApiResponseWrapper.<T>builder()
        .success(true)
        .message("Success")
        .data(data)
        .pagination(pagination)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create an error response.
   */
  public static <T> ApiResponseWrapper<T> error(String message) {
    return ApiResponseWrapper.<T>builder()
        .success(false)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create an error response with details.
   */
  public static <T> ApiResponseWrapper<T> error(String message, ErrorDetails errorDetails) {
    return ApiResponseWrapper.<T>builder()
        .success(false)
        .message(message)
        .error(errorDetails)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Create an error response with request ID.
   */
  public static <T> ApiResponseWrapper<T> error(String message, String requestId) {
    return ApiResponseWrapper.<T>builder()
        .success(false)
        .message(message)
        .requestId(requestId)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * Nested class for error details.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ErrorDetails {
    private String code;
    private String field;
    private String details;
  }

  /**
   * Nested class for pagination information.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PageInfo {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /**
     * Create PageInfo from Spring Data Page.
     */
    public static PageInfo from(org.springframework.data.domain.Page<?> page) {
      return PageInfo.builder()
          .page(page.getNumber())
          .size(page.getSize())
          .totalElements(page.getTotalElements())
          .totalPages(page.getTotalPages())
          .first(page.isFirst())
          .last(page.isLast())
          .build();
    }
  }
}
