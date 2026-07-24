package com.shoestore.modules.architecturefixture.infrastructure.persistence.entity;

import java.util.UUID;

public final class TestProductJpaEntity {

  private UUID id;
  private boolean active;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
