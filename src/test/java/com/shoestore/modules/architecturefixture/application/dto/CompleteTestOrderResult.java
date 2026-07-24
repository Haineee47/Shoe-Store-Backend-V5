package com.shoestore.modules.architecturefixture.application.dto;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.Objects;

public record CompleteTestOrderResult(
    TestOrderId orderId, boolean completed, int fulfilledLineCount) {

  public CompleteTestOrderResult {

    Objects.requireNonNull(orderId, "orderId must not be null");

    if (fulfilledLineCount < 0) {
      throw new IllegalArgumentException("fulfilledLineCount must not be negative");
    }
  }
}
