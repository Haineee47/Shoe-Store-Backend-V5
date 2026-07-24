package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationQueryStructureConventionTest {

  private static final List<Class<?>> QUERY_TYPES = List.of(FindTestProductByIdQuery.class);

  @Test
  void queriesShouldFollowNamingConvention() {

    QUERY_TYPES.forEach(queryType -> assertThat(queryType.getSimpleName()).endsWith("Query"));
  }

  @Test
  void queriesShouldBeRecordsOrFinalClasses() {

    QUERY_TYPES.forEach(
        queryType ->
            assertThat(queryType.isRecord() || Modifier.isFinal(queryType.getModifiers()))
                .isTrue());
  }

  @Test
  void queriesShouldNotDeclareMutableFields() {

    QUERY_TYPES.forEach(
        queryType -> {
          for (Field field : queryType.getDeclaredFields()) {

            if (field.isSynthetic()) {
              continue;
            }

            assertThat(Modifier.isFinal(field.getModifiers()))
                .as("%s.%s must be final", queryType.getName(), field.getName())
                .isTrue();
          }
        });
  }

  @Test
  void queriesShouldNotDeclareExecutionOrMutationMethods() {

    List<String> forbiddenMethodNames =
        List.of("execute", "handle", "save", "delete", "update", "create", "publish", "dispatch");

    QUERY_TYPES.forEach(
        queryType -> {
          List<String> methodNames =
              List.of(queryType.getDeclaredMethods()).stream()
                  .filter(method -> !method.isSynthetic())
                  .map(Method::getName)
                  .toList();

          assertThat(methodNames).doesNotContainAnyElementsOf(forbiddenMethodNames);
        });
  }
}
