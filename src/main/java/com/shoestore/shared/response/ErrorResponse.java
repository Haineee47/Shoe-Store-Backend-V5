package com.shoestore.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Standard response structure for API errors.
 *
 * @param success always false for error responses
 * @param status HTTP status code
 * @param error HTTP status description
 * @param message human-readable error message
 * @param errorCode stable machine-readable error code
 * @param path request path that caused the error
 * @param timestamp time when the error response was created
 * @param errors optional field-level validation errors
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    boolean success,
    int status,
    String error,
    String message,
    String errorCode,
    String path,
    Instant timestamp,
    Map<String, String> errors) {

  /** Creates a standard API error without field-level validation details. */
  public static ErrorResponse of(
      int status, String error, String message, String errorCode, String path) {
    return new ErrorResponse(false, status, error, message, errorCode, path, Instant.now(), null);
  }

  /** Creates an API error containing field-level validation details. */
  public static ErrorResponse validation(
      int status,
      String error,
      String message,
      String errorCode,
      String path,
      Map<String, String> errors) {
    return new ErrorResponse(false, status, error, message, errorCode, path, Instant.now(), errors);
  }
}
