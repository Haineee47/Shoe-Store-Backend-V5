package com.shoestore.shared.persistence.relationship;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RelationshipMappingIntegrationTest {

    @Autowired
    private RelationshipParentTestRepository parentRepository;

    @Autowired
    private RelationshipChildTestRepository childRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        childRepository.deleteAllInBatch();
        parentRepository.deleteAllInBatch();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldPersistParentAndChildrenThroughCascade() {
        RelationshipParentTestEntity parent =
                new RelationshipParentTestEntity("Parent");

        RelationshipChildTestEntity firstChild =
                new RelationshipChildTestEntity("Child 1", 0);

        RelationshipChildTestEntity secondChild =
                new RelationshipChildTestEntity("Child 2", 1);

        parent.addChild(firstChild);
        parent.addChild(secondChild);

        RelationshipParentTestEntity saved =
                parentRepository.saveAndFlush(parent);

        UUID parentId = saved.getId();

        entityManager.clear();

        RelationshipParentTestEntity found =
                parentRepository.findById(parentId)
                        .orElseThrow();

        assertThat(found.getChildren())
                .hasSize(2)
                .extracting(RelationshipChildTestEntity::getName)
                .containsExactlyInAnyOrder(
                        "Child 1",
                        "Child 2"
                );

        assertThat(childRepository.count())
                .isEqualTo(2);
    }

    @Test
    void shouldPersistForeignKeyOwnershipOnChild() {
        RelationshipParentTestEntity parent =
                new RelationshipParentTestEntity("Parent");

        RelationshipChildTestEntity child =
                new RelationshipChildTestEntity("Child", 0);

        parent.addChild(child);

        parentRepository.saveAndFlush(parent);

        UUID childId = child.getId();

        entityManager.clear();

        RelationshipChildTestEntity found =
                childRepository.findById(childId)
                        .orElseThrow();

        assertThat(found.getParent()).isNotNull();
        assertThat(found.getParent().getId())
                .isEqualTo(parent.getId());
    }

    @Test
    void shouldRemoveOrphanedChild() {
        RelationshipParentTestEntity parent =
                new RelationshipParentTestEntity("Parent");

        RelationshipChildTestEntity firstChild =
                new RelationshipChildTestEntity("Child 1", 0);

        RelationshipChildTestEntity secondChild =
                new RelationshipChildTestEntity("Child 2", 1);

        parent.addChild(firstChild);
        parent.addChild(secondChild);

        parentRepository.saveAndFlush(parent);

        UUID firstChildId = firstChild.getId();
        UUID secondChildId = secondChild.getId();

        parent.removeChild(firstChild);

        parentRepository.flush();
        entityManager.clear();

        assertThat(childRepository.existsById(firstChildId))
                .isFalse();

        assertThat(childRepository.existsById(secondChildId))
                .isTrue();
    }

    @Test
    void shouldKeepManyToOneRelationshipLazy() {
        RelationshipParentTestEntity parent =
                new RelationshipParentTestEntity("Parent");

        RelationshipChildTestEntity child =
                new RelationshipChildTestEntity("Child", 0);

        parent.addChild(child);
        parentRepository.saveAndFlush(parent);

        UUID childId = child.getId();

        entityManager.clear();

        RelationshipChildTestEntity found =
                childRepository.findById(childId)
                        .orElseThrow();

        PersistenceUnitUtil persistenceUnitUtil =
                entityManager
                        .getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(
                persistenceUnitUtil.isLoaded(
                        found,
                        "parent"
                )
        ).isFalse();
    }
}
