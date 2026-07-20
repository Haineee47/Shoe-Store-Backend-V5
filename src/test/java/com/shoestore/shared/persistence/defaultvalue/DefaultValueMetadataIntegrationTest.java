package com.shoestore.shared.persistence.defaultvalue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DefaultValueMetadataIntegrationTest {

    private static final String TABLE_NAME =
            "default_value_fixtures";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applicationOwnedColumnsShouldNotHaveDatabaseDefaults() {
        assertColumnHasNoDefault("version");
        assertColumnHasNoDefault("active");
        assertColumnHasNoDefault("status");
        assertColumnHasNoDefault("retry_count");
    }

    @Test
    void databaseOwnedColumnShouldHaveCurrentTimestampDefault() {
        Map<String, Object> metadata =
                loadColumnMetadata("database_created_at");

        assertThat(metadata.get("is_nullable")).isEqualTo("NO");
        assertThat(metadata.get("data_type"))
                .isEqualTo("timestamp with time zone");

        String columnDefault =
                (String) metadata.get("column_default");

        assertThat(columnDefault)
                .isNotBlank()
                .containsIgnoringCase("CURRENT_TIMESTAMP");
    }

    private void assertColumnHasNoDefault(String columnName) {
        Map<String, Object> metadata =
                loadColumnMetadata(columnName);

        assertThat(metadata.get("is_nullable")).isEqualTo("NO");
        assertThat(metadata.get("column_default")).isNull();
    }

    private Map<String, Object> loadColumnMetadata(
            String columnName
    ) {
        return jdbcTemplate.queryForMap(
                """
                SELECT
                    data_type,
                    is_nullable,
                    column_default
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = ?
                  AND column_name = ?
                """,
                TABLE_NAME,
                columnName
        );
    }
}
