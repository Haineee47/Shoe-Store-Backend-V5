package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.modules.architecturefixture.application.command.RegisterTestUserCommand;
import com.shoestore.modules.architecturefixture.application.dto.RegisterTestUserResult;
import com.shoestore.modules.architecturefixture.application.service.RegisterTestUserUseCase;
import com.shoestore.modules.architecturefixture.application.support.FakePasswordEncoder;
import com.shoestore.modules.architecturefixture.application.support.RecordingEmailSender;
import org.junit.jupiter.api.Test;

class ApplicationPortFixtureTest {

  @Test
  void useCaseShouldCoordinateOutboundPorts() {

    FakePasswordEncoder passwordEncoder = new FakePasswordEncoder();

    RecordingEmailSender emailSender = new RecordingEmailSender();

    RegisterTestUserUseCase useCase = new RegisterTestUserUseCase(passwordEncoder, emailSender);

    RegisterTestUserResult result =
        useCase.execute(new RegisterTestUserCommand("test@example.com", "secret"));

    assertThat(result.email()).isEqualTo("test@example.com");

    assertThat(result.notificationRequested()).isTrue();

    assertThat(passwordEncoder.encodeCallCount()).isEqualTo(1);

    assertThat(passwordEncoder.lastRawPassword()).isEqualTo("secret");

    assertThat(emailSender.sendCallCount()).isEqualTo(1);

    assertThat(emailSender.lastRecipient()).isEqualTo("test@example.com");
  }

  @Test
  void shouldRejectNullPasswordEncoderPort() {

    assertThatThrownBy(() -> new RegisterTestUserUseCase(null, new RecordingEmailSender()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("passwordEncoder must not be null");
  }

  @Test
  void shouldRejectNullEmailSenderPort() {

    assertThatThrownBy(() -> new RegisterTestUserUseCase(new FakePasswordEncoder(), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("emailSender must not be null");
  }

  @Test
  void shouldRejectNullCommand() {

    RegisterTestUserUseCase useCase =
        new RegisterTestUserUseCase(new FakePasswordEncoder(), new RecordingEmailSender());

    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("command must not be null");
  }
}
