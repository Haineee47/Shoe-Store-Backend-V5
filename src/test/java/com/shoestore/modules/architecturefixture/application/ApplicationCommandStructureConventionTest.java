package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.ActivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.command.CreateTestProductCommand;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationCommandStructureConventionTest {

  private static final List<Class<?>> COMMAND_TYPES =
      List.of(ActivateTestProductCommand.class, CreateTestProductCommand.class);

  @Test
  void commandsShouldFollowNamingConvention() {

    COMMAND_TYPES.forEach(
        commandType -> assertThat(commandType.getSimpleName()).endsWith("Command"));
  }

  @Test
  void commandsShouldBeRecordsOrFinalClasses() {

    COMMAND_TYPES.forEach(
        commandType ->
            assertThat(commandType.isRecord() || Modifier.isFinal(commandType.getModifiers()))
                .isTrue());
  }

  @Test
  void commandsShouldNotDeclareMutableFields() {

    COMMAND_TYPES.forEach(
        commandType -> {
          for (Field field : commandType.getDeclaredFields()) {

            if (field.isSynthetic()) {
              continue;
            }

            assertThat(Modifier.isFinal(field.getModifiers()))
                .as("%s.%s must be final", commandType.getName(), field.getName())
                .isTrue();
          }
        });
  }

  @Test
  void commandsShouldNotDeclareExecutionMethods() {

    List<String> forbiddenMethodNames =
        List.of("execute", "handle", "save", "delete", "publish", "dispatch");

    COMMAND_TYPES.forEach(
        commandType -> {
          List<String> declaredMethodNames =
              List.of(commandType.getDeclaredMethods()).stream()
                  .filter(method -> !method.isSynthetic())
                  .map(Method::getName)
                  .toList();

          assertThat(declaredMethodNames).doesNotContainAnyElementsOf(forbiddenMethodNames);
        });
  }
}
