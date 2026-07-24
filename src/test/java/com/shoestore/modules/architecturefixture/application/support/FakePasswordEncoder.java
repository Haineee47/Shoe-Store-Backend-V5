package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.application.port.PasswordEncoderPort;
import java.util.Objects;

public final class FakePasswordEncoder implements PasswordEncoderPort {

  private int encodeCallCount;
  private String lastRawPassword;

  @Override
  public String encode(String rawPassword) {

    Objects.requireNonNull(rawPassword, "rawPassword must not be null");

    encodeCallCount++;
    lastRawPassword = rawPassword;

    return "encoded:" + rawPassword;
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {

    Objects.requireNonNull(rawPassword, "rawPassword must not be null");

    Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");

    return encodedPassword.equals("encoded:" + rawPassword);
  }

  public int encodeCallCount() {
    return encodeCallCount;
  }

  public String lastRawPassword() {
    return lastRawPassword;
  }
}
