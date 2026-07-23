package com.shoestore.shared.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.persistence.relationship.RelationshipChildTestEntity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ConstraintIndexConventionTest {

  @Test
  void shouldDeclareExplicitCompositeUniqueConstraint() {
    Table table = RelationshipChildTestEntity.class.getAnnotation(Table.class);

    assertThat(table).isNotNull();

    UniqueConstraint constraint =
        Arrays.stream(table.uniqueConstraints())
            .filter(item -> item.name().equals("uk_relationship_children_parent_position"))
            .findFirst()
            .orElseThrow();

    assertThat(constraint.columnNames()).containsExactly("parent_id", "position");
  }

  @Test
  void shouldDeclareExplicitForeignKeyIndex() {
    Table table = RelationshipChildTestEntity.class.getAnnotation(Table.class);

    Index index =
        Arrays.stream(table.indexes())
            .filter(item -> item.name().equals("idx_relationship_children_parent_id"))
            .findFirst()
            .orElseThrow();

    assertThat(index.columnList()).isEqualTo("parent_id");

    assertThat(index.unique()).isFalse();
  }

  @Test
  void shouldKeepConstraintAndIndexNamesWithinPostgresLimit() {
    Table table = RelationshipChildTestEntity.class.getAnnotation(Table.class);

    Arrays.stream(table.uniqueConstraints())
        .map(UniqueConstraint::name)
        .forEach(name -> assertThat(name.length()).isLessThanOrEqualTo(63));

    Arrays.stream(table.indexes())
        .map(Index::name)
        .forEach(name -> assertThat(name.length()).isLessThanOrEqualTo(63));
  }
}
