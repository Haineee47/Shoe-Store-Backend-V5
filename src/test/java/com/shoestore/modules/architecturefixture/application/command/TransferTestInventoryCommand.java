package com.shoestore.modules.architecturefixture.application.command;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.Objects;

public record TransferTestInventoryCommand(
    TestInventoryId sourceInventoryId, TestInventoryId destinationInventoryId, int quantity) {

  public TransferTestInventoryCommand {

    Objects.requireNonNull(sourceInventoryId, "sourceInventoryId must not be null");

    Objects.requireNonNull(destinationInventoryId, "destinationInventoryId must not be null");

    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be greater than zero");
    }
  }
}
