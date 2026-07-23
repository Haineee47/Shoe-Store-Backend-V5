package com.shoestore.shared.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestOrder;
import com.shoestore.shared.domain.exception.fixture.TestOrderErrorCode;
import com.shoestore.shared.domain.exception.fixture.TestOrderStatus;
import org.junit.jupiter.api.Test;

class InvariantViolationConventionTest {

  @Test
  void shouldRejectConfirmationWhenOrderHasNoItems() {
    TestOrder order = TestOrder.createDraft();

    TestDomainException exception = catchThrowableOfType(order::confirm, TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode()).isSameAs(TestOrderErrorCode.ORDER_HAS_NO_ITEMS);
    assertThat(exception).hasMessage("Order must contain at least one item before confirmation");
  }

  @Test
  void shouldPreserveStateWhenConfirmationInvariantFails() {
    TestOrder order = TestOrder.createDraft();

    catchThrowableOfType(order::confirm, TestDomainException.class);

    assertThat(order.status()).isSameAs(TestOrderStatus.DRAFT);
    assertThat(order.totalItemQuantity()).isZero();
  }

  @Test
  void shouldRejectNonPositiveItemQuantity() {
    TestOrder order = TestOrder.createDraft();

    TestDomainException exception =
        catchThrowableOfType(() -> order.addItem(0), TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode())
        .isSameAs(TestOrderErrorCode.ORDER_ITEM_QUANTITY_MUST_BE_POSITIVE);
    assertThat(exception).hasMessage("Order item quantity must be greater than zero");
  }

  @Test
  void shouldPreserveStateWhenItemQuantityInvariantFails() {
    TestOrder order = TestOrder.createDraft();

    catchThrowableOfType(() -> order.addItem(-1), TestDomainException.class);

    assertThat(order.totalItemQuantity()).isZero();
    assertThat(order.status()).isSameAs(TestOrderStatus.DRAFT);
  }

  @Test
  void shouldAllowConfirmationWhenAllInvariantsAreSatisfied() {
    TestOrder order = TestOrder.createDraft();
    order.addItem(2);

    order.confirm();

    assertThat(order.status()).isSameAs(TestOrderStatus.CONFIRMED);
    assertThat(order.totalItemQuantity()).isEqualTo(2);
  }

  @Test
  void shouldRejectMutationAfterConfirmation() {
    TestOrder order = TestOrder.createDraft();
    order.addItem(1);
    order.confirm();

    TestDomainException exception =
        catchThrowableOfType(() -> order.addItem(1), TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode()).isSameAs(TestOrderErrorCode.ORDER_ALREADY_CONFIRMED);
    assertThat(exception).hasMessage("A confirmed order cannot be modified");
  }

  @Test
  void shouldPreserveConfirmedOrderWhenMutationIsRejected() {
    TestOrder order = TestOrder.createDraft();
    order.addItem(1);
    order.confirm();

    catchThrowableOfType(() -> order.addItem(5), TestDomainException.class);

    assertThat(order.status()).isSameAs(TestOrderStatus.CONFIRMED);
    assertThat(order.totalItemQuantity()).isEqualTo(1);
  }
}
