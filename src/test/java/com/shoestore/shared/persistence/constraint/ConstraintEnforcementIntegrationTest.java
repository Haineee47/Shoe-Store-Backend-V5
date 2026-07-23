package com.shoestore.shared.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.shared.persistence.relationship.RelationshipChildTestEntity;
import com.shoestore.shared.persistence.relationship.RelationshipParentTestEntity;
import com.shoestore.shared.persistence.relationship.RelationshipParentTestRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConstraintEnforcementIntegrationTest {

  @Autowired private RelationshipParentTestRepository parentRepository;

  @Autowired private EntityManager entityManager;

  @BeforeEach
  void setUp() {
    entityManager
        .createNativeQuery(
            """
                DELETE FROM relationship_child_test_entities
                """)
        .executeUpdate();

    entityManager
        .createNativeQuery(
            """
                DELETE FROM relationship_parent_test_entities
                """)
        .executeUpdate();

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void shouldRejectDuplicatePositionWithinSameParent() {
    RelationshipParentTestEntity parent = new RelationshipParentTestEntity("Parent");

    parent.addChild(new RelationshipChildTestEntity("Child 1", 0));

    parent.addChild(new RelationshipChildTestEntity("Child 2", 0));

    assertThatThrownBy(() -> parentRepository.saveAndFlush(parent))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectNegativePositionAtDatabaseLevel() {
    RelationshipParentTestEntity parent = new RelationshipParentTestEntity("Parent");

    parentRepository.saveAndFlush(parent);

    UUID childId = UUID.randomUUID();

    assertThatThrownBy(
            () -> {
              entityManager
                  .createNativeQuery(
                      """
                    INSERT INTO relationship_child_test_entities (
                        id,
                        version,
                        parent_id,
                        name,
                        position,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        :id,
                        0,
                        :parentId,
                        'Invalid Child',
                        -1,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """)
                  .setParameter("id", childId)
                  .setParameter("parentId", parent.getId())
                  .executeUpdate();

              entityManager.flush();
            })
        .isInstanceOf(RuntimeException.class);
  }
}
