package com.shoestore.shared.persistence.repository;

import com.shoestore.shared.persistence.support.AuditingTestEntity;
import com.shoestore.shared.persistence.support.AuditingTestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BaseRepositoryIntegrationTest {

    @Autowired
    private AuditingTestRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAllInBatch();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldSaveAndFindEntityById() {
        AuditingTestEntity entity =
                new AuditingTestEntity("Running Shoe");

        AuditingTestEntity saved =
                repository.saveAndFlush(entity);

        entityManager.clear();

        AuditingTestEntity found = repository
                .findById(saved.getId())
                .orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Running Shoe");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCheckEntityExistence() {
        AuditingTestEntity saved = repository.saveAndFlush(
                new AuditingTestEntity("Existence Test")
        );

        boolean exists = repository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldDeleteEntity() {
        AuditingTestEntity saved = repository.saveAndFlush(
                new AuditingTestEntity("Delete Test")
        );

        repository.deleteById(saved.getId());
        repository.flush();

        assertThat(repository.existsById(saved.getId()))
                .isFalse();
    }

    @Test
    void shouldReturnPaginatedAndSortedEntities() {
        repository.save(new AuditingTestEntity("Shoe C"));
        repository.save(new AuditingTestEntity("Shoe A"));
        repository.save(new AuditingTestEntity("Shoe B"));
        repository.flush();

        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.ASC, "name")
        );

        Page<AuditingTestEntity> result =
                repository.findAll(pageable);

        assertThat(result.getContent())
                .extracting(AuditingTestEntity::getName)
                .containsExactly("Shoe A", "Shoe B");

        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void shouldFilterEntitiesUsingSpecification() {
        repository.save(new AuditingTestEntity("Running Shoe"));
        repository.save(new AuditingTestEntity("Casual Shoe"));
        repository.save(new AuditingTestEntity("Running Sandal"));
        repository.flush();

        Specification<AuditingTestEntity> nameContainsRunning =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%running%"
                        );

        Page<AuditingTestEntity> result = repository.findAll(
                nameContainsRunning,
                PageRequest.of(
                        0,
                        10,
                        Sort.by("name").ascending()
                )
        );

        assertThat(result.getContent())
                .extracting(AuditingTestEntity::getName)
                .containsExactly(
                        "Running Sandal",
                        "Running Shoe"
                );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldIncrementVersionWhenEntityIsUpdated() {
        AuditingTestEntity saved = repository.saveAndFlush(
                new AuditingTestEntity("Original Name")
        );

        Long originalVersion = saved.getVersion();

        saved.rename("Updated Name");

        AuditingTestEntity updated =
                repository.saveAndFlush(saved);

        assertThat(updated.getVersion())
                .isGreaterThan(originalVersion);
    }

    @Test
    void shouldRejectUpdateUsingStaleEntityVersion() {
        AuditingTestEntity saved = repository.saveAndFlush(
                new AuditingTestEntity("Original Name")
        );

        entityManager.clear();

        AuditingTestEntity firstCopy = repository
                .findById(saved.getId())
                .orElseThrow();

        entityManager.detach(firstCopy);

        AuditingTestEntity secondCopy = repository
                .findById(saved.getId())
                .orElseThrow();

        entityManager.detach(secondCopy);

        firstCopy.rename("First Update");
        repository.saveAndFlush(firstCopy);

        entityManager.clear();

        secondCopy.rename("Stale Update");

        assertThatThrownBy(
                () -> repository.saveAndFlush(secondCopy)
        ).isInstanceOfAny(
                OptimisticLockingFailureException.class,
                OptimisticLockException.class
        );
    }
}
