package com.shoestore.modules.architecturefixture.domain.model;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Objects;

public final class TestApplicationProduct {

  private final TestApplicationProductId id;
  private boolean active;

  public TestApplicationProduct(TestApplicationProductId id, boolean active) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.active = active;
  }

  public TestApplicationProductId id() {
    return id;
  }

  public boolean isActive() {
    return active;
  }

  public void activate() {
    if (active) {
      throw new IllegalStateException("product is already active");
    }

    active = true;
  }

  public void deactivate() {
    if (!active) {
      throw new IllegalStateException("product is already inactive");
    }

    active = false;
  }
}
