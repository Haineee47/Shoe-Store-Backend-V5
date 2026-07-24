package com.shoestore.modules.architecturefixture.application.dto;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.Objects;

public record TransferTestInventoryResult(
    TestInventoryId sourceInventoryId,
    TestInventoryId destinationInventoryId,
    int transferredQuantity,
    int sourceRemainingQuantity,
    int destinationAvailableQuantity) {

  public TransferTestInventoryResult {

    Objects.requireNonNull(sourceInventoryId, "sourceInventoryId must not be null");

    Objects.requireNonNull(destinationInventoryId, "destinationInventoryId must not be null");

    if (transferredQuantity <= 0) {
      throw new IllegalArgumentException("transferredQuantity must be greater than zero");
    }

    if (sourceRemainingQuantity < 0 || destinationAvailableQuantity < 0) {

      throw new IllegalArgumentException("inventory quantities must not be negative");
    }
  }
}
