package com.shoestore.shared.domain.exception;

import java.util.Objects;

/**
 * Base exception for business-rule violations detected by the domain model.
 *
 * <p>The message is diagnostic text only. Stable machine identity is carried by {@link
 * DomainErrorCode}.
 */
public class DomainException extends RuntimeException {

  private final DomainErrorCode errorCode;

  public DomainException(DomainErrorCode errorCode, String message) {
    super(requireMessage(message));

    this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
  }

  public final DomainErrorCode errorCode() {
    return errorCode;
  }

  private static String requireMessage(String message) {
    Objects.requireNonNull(message, "message must not be null");

    if (message.isBlank()) {
      throw new IllegalArgumentException("message must not be blank");
    }

    return message;
  }
}
