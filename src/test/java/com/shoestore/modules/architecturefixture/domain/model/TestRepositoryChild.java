package com.shoestore.modules.architecturefixture.domain.model;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryChildId;
import java.util.Objects;

/**
 * Test-only entity owned by {@link TestRepositoryAggregate}.
 *
 * <p>This entity is not an aggregate root and must not have its own Domain Repository.
 */
public final class TestRepositoryChild {

  private final TestRepositoryChildId id;
  private String description;

  public TestRepositoryChild(TestRepositoryChildId id, String description) {

    this.id = Objects.requireNonNull(id, "id must not be null");
    this.description = requireDescription(description);
  }

  public TestRepositoryChildId id() {
    return id;
  }

  public String description() {
    return description;
  }

  void updateDescription(String description) {
    this.description = requireDescription(description);
  }

  private static String requireDescription(String description) {
    Objects.requireNonNull(description, "description must not be null");

    String normalizedDescription = description.trim();

    if (normalizedDescription.isBlank()) {
      throw new IllegalArgumentException("description must not be blank");
    }

    return normalizedDescription;
  }
}
