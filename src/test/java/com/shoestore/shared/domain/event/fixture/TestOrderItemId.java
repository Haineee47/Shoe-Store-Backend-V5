package com.shoestore.shared.domain.event.fixture;

import java.util.Objects;
import java.util.UUID;

public record TestOrderItemId(UUID value) {

  public TestOrderItemId {
    Objects.requireNonNull(value, "value must not be null");
  }
}
