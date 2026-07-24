package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.service.ActivateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.CompleteTestOrderUseCase;
import com.shoestore.modules.architecturefixture.application.service.CreateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.DeactivateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.FindTestProductByIdUseCase;
import com.shoestore.modules.architecturefixture.application.service.RegisterTestUserUseCase;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationServiceStructureConventionTest {

  private static final List<Class<?>> USE_CASE_TYPES =
      List.of(
          ActivateTestProductUseCase.class,
          CreateTestProductUseCase.class,
          DeactivateTestProductUseCase.class,
          FindTestProductByIdUseCase.class,
          RegisterTestUserUseCase.class,
          CompleteTestOrderUseCase.class);

  @Test
  void useCasesShouldBeFinalClasses() {

    USE_CASE_TYPES.forEach(
        useCaseType ->
            assertThat(Modifier.isFinal(useCaseType.getModifiers()))
                .as("%s must be final", useCaseType.getName())
                .isTrue());
  }

  @Test
  void useCaseNamesShouldEndWithUseCase() {

    USE_CASE_TYPES.forEach(
        useCaseType -> assertThat(useCaseType.getSimpleName()).endsWith("UseCase"));
  }

  @Test
  void useCaseDependenciesShouldBeFinal() {

    USE_CASE_TYPES.forEach(
        useCaseType -> {
          for (Field field : useCaseType.getDeclaredFields()) {

            if (field.isSynthetic()) {
              continue;
            }

            assertThat(Modifier.isFinal(field.getModifiers()))
                .as("%s.%s must be final", useCaseType.getName(), field.getName())
                .isTrue();
          }
        });
  }

  @Test
  void useCasesShouldNotUseStaticDependencyState() {

    USE_CASE_TYPES.forEach(
        useCaseType -> {
          for (Field field : useCaseType.getDeclaredFields()) {

            if (field.isSynthetic()) {
              continue;
            }

            assertThat(Modifier.isStatic(field.getModifiers()))
                .as("%s.%s must not be static", useCaseType.getName(), field.getName())
                .isFalse();
          }
        });
  }
}
