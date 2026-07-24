package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.service.CompleteTestOrderUseCase;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationServiceDependencyBoundaryTest {

  private static final List<String> FORBIDDEN_PACKAGE_PREFIXES =
      List.of(
          "org.springframework.web",
          "jakarta.persistence",
          "javax.persistence",
          "org.hibernate",
          "jakarta.servlet",
          "javax.servlet",
          "java.sql",
          "javax.sql",
          "com.shoestore.modules.architecturefixture.infrastructure",
          "com.shoestore.modules.architecturefixture.presentation",
          "com.shoestore.modules.architecturefixture.domain.model.internal");

  @Test
  void useCaseDependenciesShouldNotExposeForbiddenTypes() {

    for (Field field : CompleteTestOrderUseCase.class.getDeclaredFields()) {

      if (field.isSynthetic()) {
        continue;
      }

      String dependencyName = field.getType().getName();

      boolean forbidden = FORBIDDEN_PACKAGE_PREFIXES.stream().anyMatch(dependencyName::startsWith);

      assertThat(forbidden)
          .as(
              "%s must not depend on forbidden type %s",
              CompleteTestOrderUseCase.class.getName(), dependencyName)
          .isFalse();
    }
  }
}
