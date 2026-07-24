package com.shoestore.modules.architecturefixture.application.dto;

import java.util.Objects;

public record RegisterTestUserResult(String email, boolean notificationRequested) {

  public RegisterTestUserResult {
    Objects.requireNonNull(email, "email must not be null");
  }
}
