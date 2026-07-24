package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.command.RegisterTestUserCommand;
import com.shoestore.modules.architecturefixture.application.dto.RegisterTestUserResult;
import com.shoestore.modules.architecturefixture.application.port.EmailSenderPort;
import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import java.util.Objects;

public final class RegisterTestUserUseCase {

  private final PasswordEncoderPort passwordEncoder;
  private final EmailSenderPort emailSender;

  public RegisterTestUserUseCase(PasswordEncoderPort passwordEncoder, EmailSenderPort emailSender) {

    this.passwordEncoder =
        Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");

    this.emailSender = Objects.requireNonNull(emailSender, "emailSender must not be null");
  }

  public RegisterTestUserResult execute(RegisterTestUserCommand command) {

    Objects.requireNonNull(command, "command must not be null");

    String encodedPassword = passwordEncoder.encode(command.rawPassword());

    if (encodedPassword == null || encodedPassword.isBlank()) {

      throw new IllegalStateException("password encoder returned an invalid result");
    }

    emailSender.send(
        command.email(), "Registration completed", "Your test registration was completed.");

    return new RegisterTestUserResult(command.email(), true);
  }
}
