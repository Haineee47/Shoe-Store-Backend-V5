package com.shoestore.modules.architecturefixture.application.dto;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Objects;

public record ActivateTestProductResult(TestApplicationProductId productId, boolean active) {

  public ActivateTestProductResult {
    Objects.requireNonNull(productId, "productId must not be null");
  }
}
