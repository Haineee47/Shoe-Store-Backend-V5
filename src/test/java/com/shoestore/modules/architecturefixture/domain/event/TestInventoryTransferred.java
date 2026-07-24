package com.shoestore.modules.architecturefixture.domain.event;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.Objects;

public record TestInventoryTransferred(
    TestInventoryId sourceInventoryId, TestInventoryId destinationInventoryId, int quantity) {

  public TestInventoryTransferred {

    Objects.requireNonNull(sourceInventoryId, "sourceInventoryId must not be null");

    Objects.requireNonNull(destinationInventoryId, "destinationInventoryId must not be null");

    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be greater than zero");
    }
  }
}
