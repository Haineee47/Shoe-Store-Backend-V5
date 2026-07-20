package com.shoestore.shared.persistence.datatype;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataTypeMappingConventionTest {

    @Test
    void shouldMapUuidFieldAsUuid() throws Exception {
        Field field = getField("externalReferenceId");

        assertThat(field.getType())
                .isEqualTo(UUID.class);
    }

    @Test
    void shouldDeclareExplicitStringLength() throws Exception {
        Column column = getColumn("shortName");

        assertThat(column.length())
                .isEqualTo(150);
    }

    @Test
    void shouldMapLongTextExplicitly() throws Exception {
        Column column = getColumn("description");

        assertThat(column.columnDefinition())
                .isEqualTo("TEXT");
    }

    @Test
    void shouldMapMoneyWithExplicitPrecisionAndScale()
            throws Exception {
        Field field = getField("amount");
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType())
                .isEqualTo(BigDecimal.class);

        assertThat(column.precision())
                .isEqualTo(19);

        assertThat(column.scale())
                .isEqualTo(2);
    }

    @Test
    void shouldMapMeasurementWithExplicitPrecisionAndScale()
            throws Exception {
        Field field = getField("weightKg");
        Column column = field.getAnnotation(Column.class);

        assertThat(field.getType())
                .isEqualTo(BigDecimal.class);

        assertThat(column.precision())
                .isEqualTo(12);

        assertThat(column.scale())
                .isEqualTo(3);
    }

    @Test
    void shouldUseCorrectTemporalJavaTypes()
            throws Exception {
        assertThat(getField("occurredAt").getType())
                .isEqualTo(Instant.class);

        assertThat(getField("businessDate").getType())
                .isEqualTo(LocalDate.class);

        assertThat(getField("openingTime").getType())
                .isEqualTo(LocalTime.class);
    }

    @Test
    void shouldStoreEnumAsString() throws Exception {
        Field field = getField("status");

        Enumerated enumerated =
                field.getAnnotation(Enumerated.class);

        assertThat(enumerated)
                .isNotNull();

        assertThat(enumerated.value())
                .isEqualTo(EnumType.STRING);

        assertThat(getColumn("status").length())
                .isEqualTo(50);
    }

    private Field getField(String name)
            throws NoSuchFieldException {
        return DataTypeTestEntity.class
                .getDeclaredField(name);
    }

    private Column getColumn(String fieldName)
            throws NoSuchFieldException {
        return getField(fieldName)
                .getAnnotation(Column.class);
    }
}
