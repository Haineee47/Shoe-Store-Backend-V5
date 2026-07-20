package com.shoestore.shared.persistence.mapping;

import com.shoestore.shared.persistence.AuditableEntity;
import com.shoestore.shared.persistence.BaseEntity;
import com.shoestore.shared.persistence.support.AuditingTestEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMappingConventionTest {

    @Test
    void shouldDeclareExplicitEntityAndTableMapping() {
        Entity entity =
                AuditingTestEntity.class.getAnnotation(Entity.class);

        Table table =
                AuditingTestEntity.class.getAnnotation(Table.class);

        assertThat(entity).isNotNull();
        assertThat(table).isNotNull();
        assertThat(table.name())
                .isEqualTo("auditing_test_entity");
    }

    @Test
    void shouldMapIdentifierAsUuid() throws Exception {
        Field idField =
                BaseEntity.class.getDeclaredField("id");

        Column column =
                idField.getAnnotation(Column.class);

        assertThat(idField.getType())
                .isEqualTo(UUID.class);

        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("id");
        assertThat(column.nullable()).isFalse();
        assertThat(column.updatable()).isFalse();
    }

    @Test
    void shouldMapVersionColumn() throws Exception {
        Field versionField =
                BaseEntity.class.getDeclaredField("version");

        Version version =
                versionField.getAnnotation(Version.class);

        Column column =
                versionField.getAnnotation(Column.class);

        assertThat(version).isNotNull();
        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("version");
        assertThat(column.nullable()).isFalse();
    }

    @Test
    void shouldMapCreatedTimestampAsImmutableInstant()
            throws Exception {

        Field createdAtField =
                AuditableEntity.class.getDeclaredField("createdAt");

        Column column =
                createdAtField.getAnnotation(Column.class);

        assertThat(createdAtField.getType())
                .isEqualTo(Instant.class);

        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("created_at");
        assertThat(column.nullable()).isFalse();
        assertThat(column.updatable()).isFalse();
    }

    @Test
    void shouldMapUpdatedTimestampAsInstant()
            throws Exception {

        Field updatedAtField =
                AuditableEntity.class.getDeclaredField("updatedAt");

        Column column =
                updatedAtField.getAnnotation(Column.class);

        assertThat(updatedAtField.getType())
                .isEqualTo(Instant.class);

        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("updated_at");
        assertThat(column.nullable()).isFalse();
    }

    @Test
    void shouldDefineExplicitLengthForBoundedString()
            throws Exception {

        Field nameField =
                AuditingTestEntity.class.getDeclaredField("name");

        Column column =
                nameField.getAnnotation(Column.class);

        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("name");
        assertThat(column.nullable()).isFalse();
        assertThat(column.length()).isEqualTo(100);
    }
}
