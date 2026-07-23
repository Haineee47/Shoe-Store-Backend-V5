package com.shoestore.shared.domain.event.fixture;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record TestMoney(BigDecimal amount, Currency currency) {

  public TestMoney {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(currency, "currency must not be null");

    if (amount.signum() < 0) {
      throw new IllegalArgumentException("amount must not be negative");
    }

    amount = amount.stripTrailingZeros();
  }
}
