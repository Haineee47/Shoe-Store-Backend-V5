package com.shoestore.shared.exception;

import com.shoestore.shared.logging.ApplicationLogger;
import com.shoestore.shared.logging.ApplicationLoggerFactory;
import com.shoestore.shared.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for REST API requests.
 *
 * <p>This handler translates framework-level and application-level exceptions into the standardized
 * {@link ErrorResponse} contract.
 *
 * <p>Expected client and business errors are logged at {@code WARN} level. Unexpected server errors
 * are logged at {@code ERROR} level together with their stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String EXCEPTION_LOG_TEMPLATE =
      "Exception handled type={} errorCode={} method={} path={} status={}";

  private static final String VALIDATION_ERROR_CODE = "VALIDATION_FAILED";

  private static final String VALIDATION_ERROR_MESSAGE = "Request validation failed";

  private static final String MALFORMED_REQUEST_ERROR_CODE = "MALFORMED_REQUEST_BODY";

  private static final String MALFORMED_REQUEST_ERROR_MESSAGE =
      "Request body is missing or malformed";

  private static final String METHOD_NOT_ALLOWED_ERROR_CODE = "METHOD_NOT_ALLOWED";

  private static final String METHOD_NOT_ALLOWED_ERROR_MESSAGE =
      "HTTP method is not supported for this endpoint";

  private static final String INTERNAL_SERVER_ERROR_CODE = "INTERNAL_SERVER_ERROR";

  private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected error occurred";

  private static final String DEFAULT_FIELD_ERROR_MESSAGE = "Invalid value";

  private final ApplicationLogger logger;

  /** Creates the global exception handler using the standard application logger implementation. */
  public GlobalExceptionHandler() {
    this(ApplicationLoggerFactory.getLogger(GlobalExceptionHandler.class));
  }

  /**
   * Creates the global exception handler with the supplied logger.
   *
   * <p>Package-private visibility allows tests in the same package to inject a mock logger without
   * exposing test-specific construction publicly.
   *
   * @param logger application logger
   */
  GlobalExceptionHandler(ApplicationLogger logger) {
    this.logger = logger;
  }

  /**
   * Handles Bean Validation failures raised by request bodies annotated with {@code @Valid}.
   *
   * @param exception validation exception
   * @param request current HTTP request
   * @return standardized validation error response
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    logHandledException(exception, VALIDATION_ERROR_CODE, status, request);

    Map<String, String> fieldErrors = extractFieldErrors(exception);

    ErrorResponse response =
        ErrorResponse.validation(
            status.value(),
            status.getReasonPhrase(),
            VALIDATION_ERROR_MESSAGE,
            VALIDATION_ERROR_CODE,
            request.getRequestURI(),
            fieldErrors);

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Handles malformed, missing or unreadable JSON request bodies.
   *
   * @param exception unreadable request-body exception
   * @param request current HTTP request
   * @return standardized malformed-request response
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    logHandledException(exception, MALFORMED_REQUEST_ERROR_CODE, status, request);

    ErrorResponse response =
        ErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            MALFORMED_REQUEST_ERROR_MESSAGE,
            MALFORMED_REQUEST_ERROR_CODE,
            request.getRequestURI());

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Handles requests that use an unsupported HTTP method.
   *
   * @param exception unsupported-method exception
   * @param request current HTTP request
   * @return standardized method-not-allowed response
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

    logHandledException(exception, METHOD_NOT_ALLOWED_ERROR_CODE, status, request);

    ErrorResponse response =
        ErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            METHOD_NOT_ALLOWED_ERROR_MESSAGE,
            METHOD_NOT_ALLOWED_ERROR_CODE,
            request.getRequestURI());

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Handles expected application failures carrying structured error codes.
   *
   * @param exception application exception
   * @param request current HTTP request
   * @return standardized application error response
   */
  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ErrorResponse> handleApplicationException(
      ApplicationException exception, HttpServletRequest request) {
    ErrorCode errorCode = exception.getErrorCode();
    HttpStatus status = errorCode.getHttpStatus();

    logHandledException(exception, errorCode, request);

    ErrorResponse response =
        ErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            exception.getMessage(),
            errorCode.getCode(),
            request.getRequestURI());

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Handles all unexpected application errors.
   *
   * <p>The original exception message is intentionally not exposed because it may contain
   * implementation details or sensitive information.
   *
   * @param exception unexpected exception
   * @param request current HTTP request
   * @return standardized internal-server-error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(
      Exception exception, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    logHandledException(exception, INTERNAL_SERVER_ERROR_CODE, status, request);

    ErrorResponse response =
        ErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            INTERNAL_SERVER_ERROR_MESSAGE,
            INTERNAL_SERVER_ERROR_CODE,
            request.getRequestURI());

    return ResponseEntity.status(status).body(response);
  }

  private Map<String, String> extractFieldErrors(MethodArgumentNotValidException exception) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();

    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {

      String message = fieldError.getDefaultMessage();

      if (message == null || message.isBlank()) {
        message = DEFAULT_FIELD_ERROR_MESSAGE;
      }

      fieldErrors.putIfAbsent(fieldError.getField(), message);
    }

    return fieldErrors;
  }

  private void logHandledException(
      Exception exception, ErrorCode errorCode, HttpServletRequest request) {
    logHandledException(exception, errorCode.getCode(), errorCode.getHttpStatus(), request);
  }

  private void logHandledException(
      Exception exception, String errorCode, HttpStatus status, HttpServletRequest request) {
    if (status.is5xxServerError()) {
      logger.error(
          EXCEPTION_LOG_TEMPLATE,
          exception.getClass().getSimpleName(),
          errorCode,
          request.getMethod(),
          request.getRequestURI(),
          status.value(),
          exception);
      return;
    }

    logger.warn(
        EXCEPTION_LOG_TEMPLATE,
        exception.getClass().getSimpleName(),
        errorCode,
        request.getMethod(),
        request.getRequestURI(),
        status.value());
  }
}
