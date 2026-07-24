package com.shoestore.modules.architecturefixture.application.command;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Objects;

public record ActivateTestProductCommand(TestApplicationProductId productId) {

  public ActivateTestProductCommand {
    Objects.requireNonNull(productId, "productId must not be null");
  }
}
