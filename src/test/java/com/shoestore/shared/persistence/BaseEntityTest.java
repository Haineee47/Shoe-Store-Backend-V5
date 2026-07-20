package com.shoestore.shared.persistence;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    @Test
    void shouldNotConsiderTwoTransientEntitiesEqual() {
        TestEntity first = new TestEntity();
        TestEntity second = new TestEntity();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldConsiderSameEntityInstanceEqual() {
        TestEntity entity = new TestEntity();

        assertThat(entity).isEqualTo(entity);
    }

    @Test
    void shouldConsiderEntitiesWithSameIdentifierEqual()
            throws Exception {

        UUID identifier = UUID.randomUUID();

        TestEntity first = new TestEntity();
        TestEntity second = new TestEntity();

        setIdentifier(first, identifier);
        setIdentifier(second, identifier);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldReportEntityAsPersistedWhenIdentifierExists()
            throws Exception {

        TestEntity entity = new TestEntity();

        assertThat(entity.isPersisted()).isFalse();

        setIdentifier(entity, UUID.randomUUID());

        assertThat(entity.isPersisted()).isTrue();
    }

    private void setIdentifier(
            BaseEntity entity,
            UUID identifier
    ) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, identifier);
    }

    private static final class TestEntity extends BaseEntity {
    }
}
