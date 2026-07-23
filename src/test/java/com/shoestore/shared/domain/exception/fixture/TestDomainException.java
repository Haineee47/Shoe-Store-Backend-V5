package com.shoestore.shared.domain.exception.fixture;

import java.io.Serial;
import java.util.Objects;

/**
 * Test-only fixture representing the planned Domain Exception convention.
 *
 * <p>A domain exception represents a known business-rule rejection. It is framework-independent and
 * carries a typed domain error code.
 */
public final class TestDomainException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final TestDomainErrorCode errorCode;

  public TestDomainException(TestDomainErrorCode errorCode, String message) {
    super(requireMessage(message));
    this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
  }

  public TestDomainErrorCode errorCode() {
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
