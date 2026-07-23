package com.shoestore.shared.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.shoestore.shared.persistence.support.AuditingTestEntity;
import com.shoestore.shared.persistence.support.AuditingTestRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JpaAuditingIntegrationTest {

  @Autowired private AuditingTestRepository repository;

  @Autowired private EntityManager entityManager;

  @Test
  void shouldPopulateIdentifierVersionAndAuditTimestampsOnInsert() {
    AuditingTestEntity entity = new AuditingTestEntity("Initial name");

    AuditingTestEntity saved = repository.saveAndFlush(entity);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getVersion()).isZero();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
    assertThat(saved.isPersisted()).isTrue();
  }

  @Test
  void shouldUpdateAuditTimestampAndVersionOnUpdate() {
    AuditingTestEntity saved = repository.saveAndFlush(new AuditingTestEntity("Initial name"));

    Instant originalCreatedAt = saved.getCreatedAt();

    Instant originalUpdatedAt = saved.getUpdatedAt();

    long originalVersion = saved.getVersion();

    entityManager.clear();

    AuditingTestEntity persisted = repository.findById(saved.getId()).orElseThrow();

    persisted.rename("Updated name");

    repository.saveAndFlush(persisted);
    entityManager.clear();

    AuditingTestEntity updated = repository.findById(saved.getId()).orElseThrow();

    assertThat(updated.getName()).isEqualTo("Updated name");

    assertThat(updated.getCreatedAt()).isCloseTo(originalCreatedAt, within(1, ChronoUnit.MICROS));

    assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);

    assertThat(updated.getVersion()).isGreaterThan(originalVersion);
  }
}
