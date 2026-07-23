package com.shoestore.shared.persistence.column;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ColumnDefinitionConventionTest {

  @Test
  void shouldDeclareExplicitColumnNames() throws Exception {
    assertThat(getColumn("requiredCode").name()).isEqualTo("required_code");

    assertThat(getColumn("optionalLabel").name()).isEqualTo("optional_label");

    assertThat(getColumn("description").name()).isEqualTo("description");

    assertThat(getColumn("active").name()).isEqualTo("active");

    assertThat(getColumn("quantity").name()).isEqualTo("quantity");

    assertThat(getColumn("amount").name()).isEqualTo("amount");
  }

  @Test
  void shouldDeclareRequiredColumnsAsNotNullable() throws Exception {
    assertThat(getColumn("requiredCode").nullable()).isFalse();

    assertThat(getColumn("active").nullable()).isFalse();

    assertThat(getColumn("quantity").nullable()).isFalse();

    assertThat(getColumn("amount").nullable()).isFalse();
  }

  @Test
  void shouldKeepOptionalColumnNullable() throws Exception {
    assertThat(getColumn("optionalLabel").nullable()).isTrue();

    assertThat(getColumn("description").nullable()).isTrue();
  }

  @Test
  void shouldDeclareExplicitStringLengths() throws Exception {
    assertThat(getColumn("requiredCode").length()).isEqualTo(100);

    assertThat(getColumn("optionalLabel").length()).isEqualTo(150);
  }

  @Test
  void shouldUseTextOnlyForLongContent() throws Exception {
    assertThat(getColumn("description").columnDefinition()).isEqualTo("TEXT");

    assertThat(getColumn("requiredCode").columnDefinition()).isEmpty();

    assertThat(getColumn("optionalLabel").columnDefinition()).isEmpty();
  }

  @Test
  void shouldDeclareNumericPrecisionAndScale() throws Exception {
    Column column = getColumn("amount");

    assertThat(column.precision()).isEqualTo(19);

    assertThat(column.scale()).isEqualTo(2);
  }

  @Test
  void shouldNotOverrideInsertableAndUpdatableDefaults() throws Exception {
    for (String fieldName :
        new String[] {
          "requiredCode", "optionalLabel", "description", "active", "quantity", "amount"
        }) {
      Column column = getColumn(fieldName);

      assertThat(column.insertable()).isTrue();

      assertThat(column.updatable()).isTrue();
    }
  }

  private Column getColumn(String fieldName) throws NoSuchFieldException {
    Field field = ColumnDefinitionTestEntity.class.getDeclaredField(fieldName);

    return field.getAnnotation(Column.class);
  }
}
