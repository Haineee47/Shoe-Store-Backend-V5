package com.shoestore.shared.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.exception.fixture.TestApplicationErrorCode;
import com.shoestore.shared.domain.exception.fixture.TestApplicationException;
import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestInventoryErrorCode;
import com.shoestore.shared.domain.exception.fixture.TestOrderDomainExceptionTranslator;
import com.shoestore.shared.domain.exception.fixture.TestOrderErrorCode;
import org.junit.jupiter.api.Test;

class DomainExceptionTranslationBoundaryTest {

  private final TestOrderDomainExceptionTranslator translator =
      new TestOrderDomainExceptionTranslator();

  @Test
  void shouldTranslateConfirmationInvariantViolation() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_HAS_NO_ITEMS, "Order must contain at least one item");

    TestApplicationException applicationException = translator.translate(domainException);

    assertThat(applicationException.errorCode())
        .isSameAs(TestApplicationErrorCode.ORDER_CONFIRMATION_REJECTED);

    assertThat(applicationException).hasMessage("Order confirmation was rejected");
  }

  @Test
  void shouldTranslateInvalidConfirmationTransition() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_CANNOT_BE_CONFIRMED, "Only a draft order can be confirmed");

    TestApplicationException applicationException = translator.translate(domainException);

    assertThat(applicationException.errorCode())
        .isSameAs(TestApplicationErrorCode.ORDER_CONFIRMATION_REJECTED);
  }

  @Test
  void shouldTranslateCancellationRejection() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, "Only a draft order can be cancelled");

    TestApplicationException applicationException = translator.translate(domainException);

    assertThat(applicationException.errorCode())
        .isSameAs(TestApplicationErrorCode.ORDER_CANCELLATION_REJECTED);

    assertThat(applicationException).hasMessage("Order cancellation was rejected");
  }

  @Test
  void shouldTranslateOrderModificationRejection() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_ALREADY_CONFIRMED, "A confirmed order cannot be modified");

    TestApplicationException applicationException = translator.translate(domainException);

    assertThat(applicationException.errorCode())
        .isSameAs(TestApplicationErrorCode.ORDER_MODIFICATION_REJECTED);
  }

  @Test
  void shouldPreserveOriginalDomainExceptionAsCause() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_HAS_NO_ITEMS, "Order must contain at least one item");

    TestApplicationException applicationException = translator.translate(domainException);

    assertThat(applicationException.getCause()).isSameAs(domainException);
  }

  @Test
  void shouldPreserveOriginalDomainErrorCodeThroughCause() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, "Only a draft order can be cancelled");

    TestApplicationException applicationException = translator.translate(domainException);

    TestDomainException cause = (TestDomainException) applicationException.getCause();

    assertThat(cause.errorCode()).isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED);
  }

  @Test
  void shouldNotExposeRawDomainMessageAsApplicationMessage() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_HAS_NO_ITEMS, "Internal diagnostic domain message");

    TestApplicationException applicationException = translator.translate(domainException);

    assertThat(applicationException.getMessage()).isEqualTo("Order confirmation was rejected");

    assertThat(applicationException.getMessage()).isNotEqualTo(domainException.getMessage());
  }

  @Test
  void shouldFailFastWhenApplicationMappingIsNotDefined() {
    TestDomainException domainException =
        new TestDomainException(
            TestOrderErrorCode.ORDER_CANNOT_BE_COMPLETED,
            "Only a confirmed order can be completed");

    assertThatIllegalStateException()
        .isThrownBy(() -> translator.translate(domainException))
        .withMessageContaining("No application mapping defined")
        .withMessageContaining("ORDER_CANNOT_BE_COMPLETED");
  }

  @Test
  void shouldRejectUnsupportedModuleErrorCode() {
    TestDomainException domainException =
        new TestDomainException(
            TestInventoryErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY,
            "Requested quantity exceeds availability");

    assertThatIllegalArgumentException()
        .isThrownBy(() -> translator.translate(domainException))
        .withMessageContaining("Unsupported domain error-code type");
  }

  @Test
  void shouldRejectNullDomainException() {
    assertThatNullPointerException()
        .isThrownBy(() -> translator.translate(null))
        .withMessage("exception must not be null");
  }
}
