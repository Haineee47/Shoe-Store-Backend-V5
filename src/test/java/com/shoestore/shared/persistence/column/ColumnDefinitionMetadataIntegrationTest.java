package com.shoestore.shared.persistence.column;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ColumnDefinitionMetadataIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateExpectedColumnMetadata() {
        Map<String, ColumnMetadata> metadata =
                loadColumnMetadata();

        assertColumn(
                metadata.get("required_code"),
                "character varying",
                "NO",
                100,
                null,
                null
        );

        assertColumn(
                metadata.get("optional_label"),
                "character varying",
                "YES",
                150,
                null,
                null
        );

        assertColumn(
                metadata.get("description"),
                "text",
                "YES",
                null,
                null,
                null
        );

        assertColumn(
                metadata.get("active"),
                "boolean",
                "NO",
                null,
                null,
                null
        );

        assertColumn(
                metadata.get("quantity"),
                "integer",
                "NO",
                null,
                32,
                0
        );

        assertColumn(
                metadata.get("amount"),
                "numeric",
                "NO",
                null,
                19,
                2
        );
    }

    @Test
    void shouldNotCreateUnexpectedDatabaseDefaults() {
        List<String> columnsWithDefaults =
        entityManager
                .createNativeQuery("""
                        SELECT column_name
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name =
                              'column_definition_test_entities'
                          AND column_name IN (
                              'required_code',
                              'optional_label',
                              'description',
                              'active',
                              'quantity',
                              'amount'
                          )
                          AND column_default IS NOT NULL
                        """)
                .getResultStream()
                .map(String::valueOf)
                .toList();

        assertThat(columnsWithDefaults)
                .isEmpty();
    }

    private Map<String, ColumnMetadata>
    loadColumnMetadata() {
        List<?> rows = entityManager
                .createNativeQuery("""
                        SELECT
                            column_name,
                            data_type,
                            is_nullable,
                            character_maximum_length,
                            numeric_precision,
                            numeric_scale
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name =
                              'column_definition_test_entities'
                        """)
                .getResultList();

        return rows.stream()
                .map(Object[].class::cast)
                .map(this::toMetadata)
                .collect(
                        Collectors.toMap(
                                ColumnMetadata::name,
                                Function.identity()
                        )
                );
    }

    private ColumnMetadata toMetadata(Object[] row) {
        return new ColumnMetadata(
                String.valueOf(row[0]),
                String.valueOf(row[1]),
                String.valueOf(row[2]),
                toInteger(row[3]),
                toInteger(row[4]),
                toInteger(row[5])
        );
    }

    private Integer toInteger(Object value) {
        return value == null
                ? null
                : ((Number) value).intValue();
    }

    private void assertColumn(
            ColumnMetadata metadata,
            String expectedType,
            String expectedNullable,
            Integer expectedLength,
            Integer expectedPrecision,
            Integer expectedScale
    ) {
        assertThat(metadata)
                .isNotNull();

        assertThat(metadata.dataType())
                .isEqualTo(expectedType);

        assertThat(metadata.nullable())
                .isEqualTo(expectedNullable);

        assertThat(metadata.length())
                .isEqualTo(expectedLength);

        assertThat(metadata.precision())
                .isEqualTo(expectedPrecision);

        assertThat(metadata.scale())
                .isEqualTo(expectedScale);
    }

    private record ColumnMetadata(
            String name,
            String dataType,
            String nullable,
            Integer length,
            Integer precision,
            Integer scale
    ) {
    }
}
