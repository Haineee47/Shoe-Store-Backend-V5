package com.shoestore.modules.architecturefixture.domain.valueobject;

import java.util.Locale;
import java.util.Objects;

/**
 * Domain lookup value used to verify that repository methods accept domain types rather than raw
 * persistence-oriented values.
 */
public record TestRepositoryLookupKey(String value) {

  public TestRepositoryLookupKey {
    Objects.requireNonNull(value, "value must not be null");

    String normalizedValue = value.trim().toLowerCase(Locale.ROOT);

    if (normalizedValue.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }

    value = normalizedValue;
  }
}
