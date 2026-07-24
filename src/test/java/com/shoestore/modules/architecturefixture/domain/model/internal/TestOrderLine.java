package com.shoestore.modules.architecturefixture.domain.model.internal;

import java.util.Objects;

public final class TestOrderLine {

  private final String productCode;
  private final int quantity;
  private boolean fulfilled;

  public TestOrderLine(String productCode, int quantity) {

    this.productCode = Objects.requireNonNull(productCode, "productCode must not be null");

    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be greater than zero");
    }

    this.quantity = quantity;
  }

  public String productCode() {
    return productCode;
  }

  public int quantity() {
    return quantity;
  }

  public boolean isFulfilled() {
    return fulfilled;
  }

  public void fulfill() {
    if (fulfilled) {
      throw new IllegalStateException("order line is already fulfilled");
    }

    fulfilled = true;
  }
}
