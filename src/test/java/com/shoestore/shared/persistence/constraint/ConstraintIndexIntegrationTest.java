package com.shoestore.shared.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConstraintIndexIntegrationTest {

  @Autowired private EntityManager entityManager;

  @Test
  void shouldCreateExpectedPrimaryKeyConstraints() {
    List<String> names =
        getStringResults(
            """
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name IN (
                      'relationship_parent_test_entities',
                      'relationship_child_test_entities'
                  )
                  AND constraint_type = 'PRIMARY KEY'
                ORDER BY constraint_name
                """);

    assertThat(names)
        .contains("pk_relationship_parent_test_entities", "pk_relationship_child_test_entities");
  }

  @Test
  void shouldCreateExpectedForeignKeyConstraint() {
    List<String> names =
        getStringResults(
            """
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name =
                      'relationship_child_test_entities'
                  AND constraint_type = 'FOREIGN KEY'
                """);

    assertThat(names).contains("fk_relationship_children_parent");
  }

  @Test
  void shouldCreateExpectedUniqueConstraint() {
    List<String> names =
        getStringResults(
            """
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name =
                      'relationship_child_test_entities'
                  AND constraint_type = 'UNIQUE'
                """);

    assertThat(names).contains("uk_relationship_children_parent_position");
  }

  @Test
  void shouldCreateExpectedCheckConstraint() {
    List<String> names =
        getStringResults(
            """
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name =
                      'relationship_child_test_entities'
                  AND constraint_type = 'CHECK'
                """);

    assertThat(names).contains("ck_relationship_children_position_non_negative");
  }

  @Test
  void shouldCreateExpectedForeignKeyIndex() {
    List<String> names =
        getStringResults(
            """
                SELECT indexname
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND tablename =
                      'relationship_child_test_entities'
                """);

    assertThat(names).contains("idx_relationship_children_parent_id");
  }

  private List<String> getStringResults(String sql) {
    return entityManager.createNativeQuery(sql).getResultStream().map(String::valueOf).toList();
  }
}
