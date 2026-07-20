package com.shoestore.shared.persistence.defaultvalue;

import com.shoestore.shared.persistence.defaultvalue.fixture.DefaultValueFixtureEntity;
import com.shoestore.shared.persistence.defaultvalue.fixture.DefaultValueFixtureRepository;
import com.shoestore.shared.persistence.defaultvalue.fixture.DefaultValueFixtureStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DefaultValuePersistenceIntegrationTest {

    @Autowired
    private DefaultValueFixtureRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPersistApplicationOwnedDefaults() {
        DefaultValueFixtureEntity entity =
                new DefaultValueFixtureEntity("application-default");

        repository.save(entity);
        entityManager.flush();

        UUID id = entity.getId();

        assertThat(id).isNotNull();
        assertThat(entity.getVersion()).isZero();

        entityManager.clear();

        DefaultValueFixtureEntity reloaded =
                repository.findById(id).orElseThrow();

        assertThat(reloaded.isActive()).isTrue();
        assertThat(reloaded.getStatus())
                .isEqualTo(DefaultValueFixtureStatus.PENDING);
        assertThat(reloaded.getRetryCount()).isZero();
        assertThat(reloaded.getVersion()).isZero();
    }

    @Test
    void shouldGenerateDatabaseOwnedDefault() {
        DefaultValueFixtureEntity entity =
                new DefaultValueFixtureEntity("database-default");

        Instant beforeInsert = Instant.now();

        repository.save(entity);
        entityManager.flush();

        UUID id = entity.getId();

        assertThat(id).isNotNull();

        entityManager.clear();

        DefaultValueFixtureEntity reloaded =
                repository.findById(id).orElseThrow();

        Instant afterReload = Instant.now();

        assertThat(reloaded.getDatabaseCreatedAt()).isNotNull();
        assertThat(reloaded.getDatabaseCreatedAt())
                .isBetween(beforeInsert, afterReload);
    }

    @Test
    void nativeInsertShouldUseDatabaseDefaultWhenColumnIsOmitted() {
        UUID id = UUID.randomUUID();
        Instant beforeInsert = Instant.now();

        jdbcTemplate.update(
                """
                INSERT INTO default_value_fixtures
                    (
                        id,
                        version,
                        name,
                        active,
                        status,
                        retry_count
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """,
                id,
                0L,
                "native-default",
                true,
                "PENDING",
                0
        );

        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT database_created_at
                FROM default_value_fixtures
                WHERE id = ?
                """,
                id
        );

        Timestamp timestamp =
                (Timestamp) row.get("database_created_at");

        assertThat(timestamp).isNotNull();
        assertThat(timestamp.toInstant())
                .isAfterOrEqualTo(beforeInsert);
    }

    @Test
    void explicitNullShouldNotBeReplacedByDatabaseDefault() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO default_value_fixtures
                            (
                                id,
                                version,
                                name,
                                active,
                                status,
                                retry_count,
                                database_created_at
                            )
                        VALUES
                            (?, ?, ?, ?, ?, ?, NULL)
                        """,
                        id,
                        0L,
                        "explicit-null",
                        true,
                        "PENDING",
                        0
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
