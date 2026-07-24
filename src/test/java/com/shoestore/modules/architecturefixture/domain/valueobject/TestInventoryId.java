package com.shoestore.modules.architecturefixture.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TestInventoryId(UUID value) {

  public TestInventoryId {
    Objects.requireNonNull(value, "value must not be null");
  }

  public static TestInventoryId generate() {
    return new TestInventoryId(UUID.randomUUID());
  }
}
