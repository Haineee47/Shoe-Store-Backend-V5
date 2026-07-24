package com.shoestore.modules.architecturefixture.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed identity of a child entity owned by the aggregate repository fixture.
 */
public record TestRepositoryChildId(UUID value) {

  public TestRepositoryChildId {
    Objects.requireNonNull(value, "value must not be null");
  }

  public static TestRepositoryChildId generate() {
    return new TestRepositoryChildId(UUID.randomUUID());
  }
}
