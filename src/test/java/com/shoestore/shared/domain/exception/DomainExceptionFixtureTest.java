package com.shoestore.shared.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestOrderErrorCode;
import org.junit.jupiter.api.Test;

class DomainExceptionFixtureTest {

  @Test
  void shouldBeAnUncheckedException() {
    TestDomainException exception =
        new TestDomainException(
            TestOrderErrorCode.ORDER_ALREADY_CONFIRMED, "Order is already confirmed");

    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldPreserveTypedDomainErrorCode() {
    TestDomainException exception =
        new TestDomainException(
            TestOrderErrorCode.ORDER_ALREADY_CONFIRMED, "Order is already confirmed");

    assertThat(exception.errorCode()).isSameAs(TestOrderErrorCode.ORDER_ALREADY_CONFIRMED);
  }

  @Test
  void shouldPreserveDiagnosticMessage() {
    TestDomainException exception =
        new TestDomainException(
            TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, "A confirmed order cannot be cancelled");

    assertThat(exception).hasMessage("A confirmed order cannot be cancelled");
  }

  @Test
  void shouldRejectNullErrorCode() {
    assertThatNullPointerException()
        .isThrownBy(() -> new TestDomainException(null, "Order cannot be cancelled"))
        .withMessage("errorCode must not be null");
  }

  @Test
  void shouldRejectNullMessage() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> new TestDomainException(TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, null))
        .withMessage("message must not be null");
  }

  @Test
  void shouldRejectEmptyMessage() {
    assertThatThrownBy(
            () -> new TestDomainException(TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("message must not be blank");
  }

  @Test
  void shouldRejectWhitespaceOnlyMessage() {
    assertThatThrownBy(
            () -> new TestDomainException(TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("message must not be blank");
  }

  @Test
  void shouldNotRequireAThrowableCause() {
    TestDomainException exception =
        new TestDomainException(
            TestOrderErrorCode.ORDER_HAS_NO_ITEMS, "Order must contain at least one item");

    assertThat(exception.getCause()).isNull();
  }
}
