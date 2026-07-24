package com.shoestore.modules.architecturefixture.application.command;

import java.util.Objects;

public record RegisterTestUserCommand(String email, String rawPassword) {

  public RegisterTestUserCommand {

    Objects.requireNonNull(email, "email must not be null");

    Objects.requireNonNull(rawPassword, "rawPassword must not be null");
  }
}
