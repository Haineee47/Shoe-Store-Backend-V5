package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.port.EmailSenderPort;
import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import com.shoestore.modules.architecturefixture.domain.repository.TestApplicationProductRepository;
import org.junit.jupiter.api.Test;

class ApplicationPortSemanticsTest {

  @Test
  void applicationPortsShouldNotBeDomainRepositories() {

    assertThat(TestApplicationProductRepository.class.isAssignableFrom(PasswordEncoderPort.class))
        .isFalse();

    assertThat(TestApplicationProductRepository.class.isAssignableFrom(EmailSenderPort.class))
        .isFalse();
  }

  @Test
  void applicationPortsShouldBeInterfaces() {

    assertThat(PasswordEncoderPort.class.isInterface()).isTrue();

    assertThat(EmailSenderPort.class.isInterface()).isTrue();
  }

  @Test
  void applicationPortsShouldBeOwnedByApplicationLayer() {

    assertThat(PasswordEncoderPort.class.getPackageName()).contains(".application.port");

    assertThat(EmailSenderPort.class.getPackageName()).contains(".application.port");
  }
}
