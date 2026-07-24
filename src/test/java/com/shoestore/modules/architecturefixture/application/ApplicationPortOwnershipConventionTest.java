package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.port.EmailSenderPort;
import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationPortOwnershipConventionTest {

  private static final List<Class<?>> PORT_TYPES =
      List.of(PasswordEncoderPort.class, EmailSenderPort.class);

  @Test
  void portsShouldResideInApplicationPortPackage() {

    PORT_TYPES.forEach(
        portType -> assertThat(portType.getPackageName()).contains(".application.port"));
  }

  @Test
  void portsShouldNotResideInDomainRepositoryPackage() {

    PORT_TYPES.forEach(
        portType -> assertThat(portType.getPackageName()).doesNotContain(".domain.repository"));
  }

  @Test
  void portsShouldNotResideInInfrastructurePackage() {

    PORT_TYPES.forEach(
        portType -> assertThat(portType.getPackageName()).doesNotContain(".infrastructure."));
  }
}
