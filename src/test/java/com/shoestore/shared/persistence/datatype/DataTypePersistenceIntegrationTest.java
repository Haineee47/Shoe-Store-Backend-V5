package com.shoestore.shared.persistence.datatype;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataTypePersistenceIntegrationTest {

    @Autowired
    private DataTypeTestRepository repository;

    @Test
    void shouldPersistAndLoadSupportedDataTypes() {
        UUID externalReferenceId = UUID.randomUUID();

        Instant occurredAt =
                Instant.parse("2026-07-20T10:15:30Z");

        LocalDate businessDate =
                LocalDate.of(2026, 7, 20);

        LocalTime openingTime =
                LocalTime.of(8, 30);

        DataTypeTestEntity entity =
                new DataTypeTestEntity(
                        externalReferenceId,
                        "Data type test",
                        "Long description",
                        true,
                        10,
                        1_000L,
                        new BigDecimal("199999.99"),
                        new BigDecimal("12.345"),
                        occurredAt,
                        businessDate,
                        openingTime,
                        DataTypeTestStatus.ACTIVE
                );

        DataTypeTestEntity saved =
                repository.saveAndFlush(entity);

        DataTypeTestEntity loaded =
                repository.findById(saved.getId())
                        .orElseThrow();

        assertThat(loaded.getExternalReferenceId())
                .isEqualTo(externalReferenceId);

        assertThat(loaded.getShortName())
                .isEqualTo("Data type test");

        assertThat(loaded.getDescription())
                .isEqualTo("Long description");

        assertThat(loaded.isActive())
                .isTrue();

        assertThat(loaded.getQuantity())
                .isEqualTo(10);

        assertThat(loaded.getViewCount())
                .isEqualTo(1_000L);

        assertThat(loaded.getAmount())
                .isEqualByComparingTo(
                        new BigDecimal("199999.99")
                );

        assertThat(loaded.getWeightKg())
                .isEqualByComparingTo(
                        new BigDecimal("12.345")
                );

        assertThat(loaded.getOccurredAt())
                .isEqualTo(occurredAt);

        assertThat(loaded.getBusinessDate())
                .isEqualTo(businessDate);

        assertThat(loaded.getOpeningTime())
                .isEqualTo(openingTime);

        assertThat(loaded.getStatus())
                .isEqualTo(DataTypeTestStatus.ACTIVE);
    }
}
