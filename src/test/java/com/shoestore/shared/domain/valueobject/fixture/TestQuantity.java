package com.shoestore.shared.domain.valueobject.fixture;

import java.util.Objects;

public record TestQuantity(int value) {

  public TestQuantity {
    if (value <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
  }

  public TestQuantity increaseBy(TestQuantity amount) {
    Objects.requireNonNull(amount, "amount must not be null");

    return new TestQuantity(Math.addExact(value, amount.value));
  }
}
