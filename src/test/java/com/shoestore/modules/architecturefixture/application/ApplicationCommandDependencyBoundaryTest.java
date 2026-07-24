package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.ActivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.command.CreateTestProductCommand;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationCommandDependencyBoundaryTest {

  private static final List<Class<?>> COMMAND_TYPES =
      List.of(ActivateTestProductCommand.class, CreateTestProductCommand.class);

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
  void commandsShouldNotDependOnForbiddenTypes() {

    COMMAND_TYPES.forEach(
        commandType -> {
          for (Field field : commandType.getDeclaredFields()) {

            if (field.isSynthetic()) {
              continue;
            }

            assertTypeIsAllowed(commandType, field.getType());
          }
        });
  }

  private static void assertTypeIsAllowed(Class<?> commandType, Class<?> dependencyType) {

    if (dependencyType.isArray()) {
      assertTypeIsAllowed(commandType, dependencyType.getComponentType());
      return;
    }

    String dependencyName = dependencyType.getName();

    boolean forbidden = FORBIDDEN_PACKAGE_PREFIXES.stream().anyMatch(dependencyName::startsWith);

    assertThat(forbidden)
        .as("%s must not depend on forbidden type %s", commandType.getName(), dependencyName)
        .isFalse();
  }
}
