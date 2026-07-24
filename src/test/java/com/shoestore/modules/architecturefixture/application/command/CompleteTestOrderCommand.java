package com.shoestore.modules.architecturefixture.application.command;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.Objects;

public record CompleteTestOrderCommand(TestOrderId orderId) {

  public CompleteTestOrderCommand {
    Objects.requireNonNull(orderId, "orderId must not be null");
  }
}
