package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.port.EmailSenderPort;
import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationPortDependencyBoundaryTest {

  private static final List<Class<?>> PORT_TYPES =
      List.of(PasswordEncoderPort.class, EmailSenderPort.class);

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
          "com.shoestore.modules.architecturefixture.infrastructure",
          "com.shoestore.modules.architecturefixture.presentation");

  @Test
  void portsShouldNotExposeForbiddenTypes() {

    PORT_TYPES.forEach(
        portType -> {
          for (Method method : portType.getDeclaredMethods()) {

            assertTypeIsAllowed(portType, method.getReturnType());

            for (Class<?> parameterType : method.getParameterTypes()) {

              assertTypeIsAllowed(portType, parameterType);
            }
          }
        });
  }

  private static void assertTypeIsAllowed(Class<?> portType, Class<?> dependencyType) {

    if (dependencyType == void.class || dependencyType.isPrimitive()) {
      return;
    }

    if (dependencyType.isArray()) {
      assertTypeIsAllowed(portType, dependencyType.getComponentType());
      return;
    }

    String dependencyName = dependencyType.getName();

    boolean forbidden = FORBIDDEN_PACKAGE_PREFIXES.stream().anyMatch(dependencyName::startsWith);

    assertThat(forbidden)
        .as("%s must not expose forbidden type %s", portType.getName(), dependencyName)
        .isFalse();
  }
}
