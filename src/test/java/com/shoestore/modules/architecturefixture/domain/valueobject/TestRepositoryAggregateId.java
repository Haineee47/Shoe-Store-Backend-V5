package com.shoestore.modules.architecturefixture.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identity used by the domain repository convention fixture.
 */
public record TestRepositoryAggregateId(UUID value) {

  public TestRepositoryAggregateId {
    Objects.requireNonNull(value, "value must not be null");
  }

  public static TestRepositoryAggregateId generate() {
    return new TestRepositoryAggregateId(UUID.randomUUID());
  }
}
