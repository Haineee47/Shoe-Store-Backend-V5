package com.shoestore.shared.domain.event.fixture;

import java.util.Objects;
import java.util.UUID;

public record TestCustomerId(UUID value) {

  public TestCustomerId {
    Objects.requireNonNull(value, "value must not be null");
  }
}
