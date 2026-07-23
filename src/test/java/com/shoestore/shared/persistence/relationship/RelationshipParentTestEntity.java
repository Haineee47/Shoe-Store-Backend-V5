package com.shoestore.shared.persistence.relationship;

import com.shoestore.shared.persistence.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "relationship_parent_test_entities")
public class RelationshipParentTestEntity extends AuditableEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @OneToMany(
      mappedBy = "parent",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private final List<RelationshipChildTestEntity> children = new ArrayList<>();

  protected RelationshipParentTestEntity() {}

  public RelationshipParentTestEntity(String name) {
    this.name = requireName(name);
  }

  public String getName() {
    return name;
  }

  public List<RelationshipChildTestEntity> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public void addChild(RelationshipChildTestEntity child) {
    Objects.requireNonNull(child, "Child must not be null");

    if (children.contains(child)) {
      return;
    }

    children.add(child);
    child.attachTo(this);
  }

  public void removeChild(RelationshipChildTestEntity child) {
    if (children.remove(child)) {
      child.detachFrom(this);
    }
  }

  private static String requireName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name must not be blank");
    }

    return name.trim();
  }
}
