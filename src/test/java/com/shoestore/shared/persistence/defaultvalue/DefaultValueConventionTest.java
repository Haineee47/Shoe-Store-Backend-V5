package com.shoestore.shared.persistence.defaultvalue;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.persistence.defaultvalue.fixture.DefaultValueFixtureEntity;
import com.shoestore.shared.persistence.defaultvalue.fixture.DefaultValueFixtureStatus;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DefaultValueConventionTest {

  @Test
  void applicationOwnedDefaultsShouldBeInitializedInJava() {
    DefaultValueFixtureEntity entity = new DefaultValueFixtureEntity("fixture");

    assertThat(entity.isActive()).isTrue();
    assertThat(entity.getStatus()).isEqualTo(DefaultValueFixtureStatus.PENDING);
    assertThat(entity.getRetryCount()).isZero();
  }

  @Test
  void databaseOwnedDefaultShouldNotBeInitializedInJava() {
    DefaultValueFixtureEntity entity = new DefaultValueFixtureEntity("fixture");

    assertThat(entity.getDatabaseCreatedAt()).isNull();
  }

  @Test
  void databaseOwnedDefaultShouldNotBeInsertableOrUpdatable() throws NoSuchFieldException {

    Field field = DefaultValueFixtureEntity.class.getDeclaredField("databaseCreatedAt");

    Column column = field.getAnnotation(Column.class);

    assertThat(column).isNotNull();
    assertThat(column.name()).isEqualTo("database_created_at");
    assertThat(column.nullable()).isFalse();
    assertThat(column.insertable()).isFalse();
    assertThat(column.updatable()).isFalse();
    assertThat(field.getType()).isEqualTo(Instant.class);
  }

  @Test
  void applicationOwnedDefaultsShouldRemainInsertable() throws NoSuchFieldException {

    assertApplicationOwnedColumn("active");
    assertApplicationOwnedColumn("status");
    assertApplicationOwnedColumn("retryCount");
  }

  private static void assertApplicationOwnedColumn(String fieldName) throws NoSuchFieldException {

    Field field = DefaultValueFixtureEntity.class.getDeclaredField(fieldName);

    Column column = field.getAnnotation(Column.class);

    assertThat(column).isNotNull();
    assertThat(column.nullable()).isFalse();
    assertThat(column.insertable()).isTrue();
    assertThat(column.updatable()).isTrue();
  }
}
