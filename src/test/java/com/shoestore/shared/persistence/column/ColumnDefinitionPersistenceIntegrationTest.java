package com.shoestore.shared.persistence.column;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ColumnDefinitionPersistenceIntegrationTest {

    @Autowired
    private ColumnDefinitionTestRepository repository;

    @Test
    void shouldPersistRequiredAndOptionalColumns() {
        ColumnDefinitionTestEntity entity =
                new ColumnDefinitionTestEntity(
                        "COLUMN-001",
                        null,
                        "Long description",
                        true,
                        10,
                        new BigDecimal("120.50")
                );

        ColumnDefinitionTestEntity saved =
                repository.saveAndFlush(entity);

        ColumnDefinitionTestEntity loaded =
                repository.findById(saved.getId())
                        .orElseThrow();

        assertThat(loaded.getRequiredCode())
                .isEqualTo("COLUMN-001");

        assertThat(loaded.getOptionalLabel())
                .isNull();

        assertThat(loaded.getDescription())
                .isEqualTo("Long description");

        assertThat(loaded.isActive())
                .isTrue();

        assertThat(loaded.getQuantity())
                .isEqualTo(10);

        assertThat(loaded.getAmount())
                .isEqualByComparingTo(
                        new BigDecimal("120.50")
                );
    }
}
