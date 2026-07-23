package com.shoestore.shared.domain.exception.fixture;

/**
 * Test-only aggregate fixture used to verify invariant and state-transition conventions.
 *
 * <p>This class intentionally contains only the minimum behavior required by the architectural
 * tests. It is not a production Order model.
 */
public final class TestOrder {

  private int totalItemQuantity;
  private TestOrderStatus status;

  private TestOrder() {
    this.totalItemQuantity = 0;
    this.status = TestOrderStatus.DRAFT;
  }

  public static TestOrder createDraft() {
    return new TestOrder();
  }

  public void addItem(int quantity) {
    ensureModifiable();

    if (quantity <= 0) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_ITEM_QUANTITY_MUST_BE_POSITIVE,
          "Order item quantity must be greater than zero");
    }

    totalItemQuantity += quantity;
  }

  public void confirm() {
    if (status != TestOrderStatus.DRAFT) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_CANNOT_BE_CONFIRMED, "Only a draft order can be confirmed");
    }

    if (totalItemQuantity == 0) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_HAS_NO_ITEMS,
          "Order must contain at least one item before confirmation");
    }

    status = TestOrderStatus.CONFIRMED;
  }

  public void cancel() {
    if (status == TestOrderStatus.CANCELLED) {
      return;
    }

    if (status != TestOrderStatus.DRAFT) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED, "Only a draft order can be cancelled");
    }

    status = TestOrderStatus.CANCELLED;
  }

  public void complete() {
    if (status == TestOrderStatus.COMPLETED) {
      return;
    }

    if (status != TestOrderStatus.CONFIRMED) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_CANNOT_BE_COMPLETED, "Only a confirmed order can be completed");
    }

    status = TestOrderStatus.COMPLETED;
  }

  public int totalItemQuantity() {
    return totalItemQuantity;
  }

  public TestOrderStatus status() {
    return status;
  }

  private void ensureModifiable() {
    if (status == TestOrderStatus.CONFIRMED) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_ALREADY_CONFIRMED, "A confirmed order cannot be modified");
    }

    if (status != TestOrderStatus.DRAFT) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_CANNOT_BE_MODIFIED, "Only a draft order can be modified");
    }
  }
}
