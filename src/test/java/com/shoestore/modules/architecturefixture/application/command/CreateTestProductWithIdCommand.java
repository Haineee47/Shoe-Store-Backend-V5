package com.shoestore.modules.architecturefixture.application.command;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Objects;

public record CreateTestProductWithIdCommand(TestApplicationProductId productId) {

  public CreateTestProductWithIdCommand {
    Objects.requireNonNull(productId, "productId must not be null");
  }
}
