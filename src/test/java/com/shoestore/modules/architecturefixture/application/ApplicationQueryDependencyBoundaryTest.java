package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationQueryDependencyBoundaryTest {

  private static final List<Class<?>> QUERY_TYPES = List.of(FindTestProductByIdQuery.class);

  private static final List<String> FORBIDDEN_PACKAGE_PREFIXES =
      List.of(
          "org.springframework",
          "jakarta.persistence",
          "javax.persistence",
          "org.hibernate",
          "jakarta.servlet",
          "javax.servlet",
          "java.sql",
          "javax.sql",
          "com.shoestore.shared.persistence",
          "com.shoestore.modules.architecturefixture.infrastructure",
          "com.shoestore.modules.architecturefixture.presentation");

  @Test
  void queriesShouldNotDependOnForbiddenTypes() {

    QUERY_TYPES.forEach(
        queryType -> {
          for (Field field : queryType.getDeclaredFields()) {

            if (field.isSynthetic()) {
              continue;
            }

            assertTypeIsAllowed(queryType, field.getType());
          }
        });
  }

  private static void assertTypeIsAllowed(Class<?> queryType, Class<?> dependencyType) {

    if (dependencyType.isArray()) {
      assertTypeIsAllowed(queryType, dependencyType.getComponentType());
      return;
    }

    String dependencyName = dependencyType.getName();

    boolean forbidden = FORBIDDEN_PACKAGE_PREFIXES.stream().anyMatch(dependencyName::startsWith);

    assertThat(forbidden)
        .as("%s must not depend on forbidden type %s", queryType.getName(), dependencyName)
        .isFalse();
  }
}
