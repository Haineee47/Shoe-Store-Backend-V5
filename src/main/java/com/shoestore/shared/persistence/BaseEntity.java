package com.shoestore.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.util.Objects;
import java.util.UUID;

/**
 * Base persistence model shared by all JPA entities.
 *
 * <p>Provides a generated UUID identifier and optimistic locking support.
 */
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  public UUID getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  /**
   * Determines whether this entity has already been persisted.
   *
   * @return {@code true} when the entity has an identifier
   */
  public boolean isPersisted() {
    return id != null;
  }

  @Override
  public final boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    BaseEntity that = (BaseEntity) object;

    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }
}
