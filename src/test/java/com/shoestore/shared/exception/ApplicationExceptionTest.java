package com.shoestore.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class ApplicationExceptionTest {

  @Test
  void shouldCreateExceptionUsingDefaultErrorMessage() {
    ApplicationException exception = new ApplicationException(CommonErrorCode.RESOURCE_NOT_FOUND);

    assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo("Requested resource was not found");
  }

  @Test
  void shouldCreateExceptionUsingContextualMessage() {
    ApplicationException exception =
        new ApplicationException(CommonErrorCode.RESOURCE_NOT_FOUND, "Product was not found");

    assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo("Product was not found");
  }

  @Test
  void shouldRejectNullErrorCode() {
    assertThatNullPointerException()
        .isThrownBy(() -> new ApplicationException(null))
        .withMessage("errorCode must not be null");
  }

  @Test
  void shouldRejectBlankCustomMessage() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new ApplicationException(CommonErrorCode.INVALID_REQUEST, "   "))
        .withMessage("message must not be blank");
  }
}
