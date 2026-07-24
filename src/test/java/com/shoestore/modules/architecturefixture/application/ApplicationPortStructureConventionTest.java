package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.port.EmailSenderPort;
import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationPortStructureConventionTest {

  private static final List<Class<?>> PORT_TYPES =
      List.of(PasswordEncoderPort.class, EmailSenderPort.class);

  @Test
  void portsShouldBeInterfaces() {

    PORT_TYPES.forEach(
        portType ->
            assertThat(portType.isInterface())
                .as("%s must be an interface", portType.getName())
                .isTrue());
  }

  @Test
  void portsShouldFollowNamingConvention() {

    PORT_TYPES.forEach(portType -> assertThat(portType.getSimpleName()).endsWith("Port"));
  }

  @Test
  void portsShouldNotDeclareInstanceState() {

    PORT_TYPES.forEach(
        portType -> {
          for (Field field : portType.getDeclaredFields()) {

            assertThat(Modifier.isStatic(field.getModifiers()))
                .as("%s.%s must not be instance state", portType.getName(), field.getName())
                .isTrue();
          }
        });
  }
}
