package com.shoestore.shared.domain.service.fixture;

/**
 * Non-negative quantity used by the Domain Service architecture fixtures.
 *
 * <p>Zero is valid because an allocation result, remaining quantity or unfulfilled quantity may
 * legitimately be zero.
 */
public record TestAllocationQuantity(int value) {

  public TestAllocationQuantity {
    if (value < 0) {
      throw new IllegalArgumentException("allocation quantity must not be negative");
    }
  }

  public boolean isZero() {
    return value == 0;
  }

  public TestAllocationQuantity subtract(TestAllocationQuantity amount) {
    if (amount == null) {
      throw new NullPointerException("amount must not be null");
    }

    int result = Math.subtractExact(value, amount.value);

    return new TestAllocationQuantity(result);
  }
}
