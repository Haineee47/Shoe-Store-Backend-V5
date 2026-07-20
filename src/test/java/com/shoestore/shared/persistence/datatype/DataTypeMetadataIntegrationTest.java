package com.shoestore.shared.persistence.datatype;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataTypeMetadataIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateExpectedPostgresColumnTypes() {
        Map<String, String> columnTypes = ((java.util.List<Object[]>) entityManager
                .createNativeQuery("""
                        SELECT
                            column_name,
                            data_type
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                        AND table_name =
                            'data_type_test_entities'
                        """)
                .getResultList())
                .stream()
                .collect(
                        Collectors.toMap(
                                row -> String.valueOf(row[0]),
                                row -> String.valueOf(row[1])));

        assertThat(columnTypes)
                .containsEntry(
                        "external_reference_id",
                        "uuid")
                .containsEntry(
                        "short_name",
                        "character varying")
                .containsEntry(
                        "description",
                        "text")
                .containsEntry(
                        "active",
                        "boolean")
                .containsEntry(
                        "quantity",
                        "integer")
                .containsEntry(
                        "view_count",
                        "bigint")
                .containsEntry(
                        "amount",
                        "numeric")
                .containsEntry(
                        "weight_kg",
                        "numeric")
                .containsEntry(
                        "occurred_at",
                        "timestamp with time zone")
                .containsEntry(
                        "business_date",
                        "date")
                .containsEntry(
                        "opening_time",
                        "time without time zone")
                .containsEntry(
                        "status",
                        "character varying");
    }

    @Test
    void shouldCreateExpectedNumericPrecisionAndScale() {
        Object[] amountMetadata =
                (Object[]) entityManager
                        .createNativeQuery("""
                                SELECT
                                    numeric_precision,
                                    numeric_scale
                                FROM information_schema.columns
                                WHERE table_schema = 'public'
                                AND table_name =
                                    'data_type_test_entities'
                                AND column_name = 'amount'
                                """)
                        .getSingleResult();

        assertThat(
                ((Number) amountMetadata[0]).intValue()
        ).isEqualTo(19);

        assertThat(
                ((Number) amountMetadata[1]).intValue()
        ).isEqualTo(2);

        Object[] weightMetadata =
                (Object[]) entityManager
                        .createNativeQuery("""
                                SELECT
                                    numeric_precision,
                                    numeric_scale
                                FROM information_schema.columns
                                WHERE table_schema = 'public'
                                AND table_name =
                                    'data_type_test_entities'
                                AND column_name = 'weight_kg'
                                """)
                        .getSingleResult();

        assertThat(
                ((Number) weightMetadata[0]).intValue()
        ).isEqualTo(12);

        assertThat(
                ((Number) weightMetadata[1]).intValue()
        ).isEqualTo(3);
    }
}
