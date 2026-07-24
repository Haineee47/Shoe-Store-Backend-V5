package com.shoestore.modules.architecturefixture.infrastructure.security;

import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import java.util.Objects;

public final class TestPasswordEncoderAdapter implements PasswordEncoderPort {

  @Override
  public String encode(String rawPassword) {

    Objects.requireNonNull(rawPassword, "rawPassword must not be null");

    return "infrastructure-encoded:" + rawPassword;
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {

    Objects.requireNonNull(rawPassword, "rawPassword must not be null");

    Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");

    return encodedPassword.equals("infrastructure-encoded:" + rawPassword);
  }
}
