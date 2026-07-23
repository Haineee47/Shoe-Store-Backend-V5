package com.shoestore.shared.persistence.defaultvalue.fixture;

import com.shoestore.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "default_value_fixtures")
public class DefaultValueFixtureEntity extends BaseEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private DefaultValueFixtureStatus status = DefaultValueFixtureStatus.PENDING;

  @Column(name = "retry_count", nullable = false)
  private int retryCount = 0;

  @Column(name = "database_created_at", nullable = false, insertable = false, updatable = false)
  private Instant databaseCreatedAt;

  protected DefaultValueFixtureEntity() {}

  public DefaultValueFixtureEntity(String name) {
    this.name = requireText(name, "name");
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return active;
  }

  public DefaultValueFixtureStatus getStatus() {
    return status;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public Instant getDatabaseCreatedAt() {
    return databaseCreatedAt;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }

    return value;
  }
}
