package com.shoestore.modules.architecturefixture.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TestApplicationProductId(UUID value) {

  public TestApplicationProductId {
    Objects.requireNonNull(value, "value must not be null");
  }

  public static TestApplicationProductId generate() {
    return new TestApplicationProductId(UUID.randomUUID());
  }
}
