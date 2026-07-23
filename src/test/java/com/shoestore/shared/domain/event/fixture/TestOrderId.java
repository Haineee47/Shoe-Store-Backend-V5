package com.shoestore.shared.domain.event.fixture;

import java.util.Objects;
import java.util.UUID;

public record TestOrderId(UUID value) {

  public TestOrderId {
    Objects.requireNonNull(value, "value must not be null");
  }
}
