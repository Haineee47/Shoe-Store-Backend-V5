package com.shoestore.modules.architecturefixture.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TestOrderId(UUID value) {

  public TestOrderId {
    Objects.requireNonNull(value, "value must not be null");
  }

  public static TestOrderId generate() {
    return new TestOrderId(UUID.randomUUID());
  }
}
