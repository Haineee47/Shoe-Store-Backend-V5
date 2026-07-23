package com.shoestore.shared.domain.service.fixture;

import java.util.Objects;

/**
 * Stateless Domain Service fixture that determines how much of a requested quantity can be
 * allocated from an available quantity.
 */
public final class TestAllocationPolicy {

  public TestAllocationDecision allocate(
      TestAllocationQuantity requestedQuantity, TestAllocationQuantity availableQuantity) {
    Objects.requireNonNull(requestedQuantity, "requestedQuantity must not be null");

    Objects.requireNonNull(availableQuantity, "availableQuantity must not be null");

    if (requestedQuantity.isZero()) {
      throw new IllegalArgumentException("requestedQuantity must be greater than zero");
    }

    int allocatedValue = Math.min(requestedQuantity.value(), availableQuantity.value());

    TestAllocationQuantity allocatedQuantity = new TestAllocationQuantity(allocatedValue);

    TestAllocationQuantity remainingAvailableQuantity =
        availableQuantity.subtract(allocatedQuantity);

    TestAllocationQuantity unfulfilledQuantity = requestedQuantity.subtract(allocatedQuantity);

    TestAllocationStatus status = determineStatus(allocatedQuantity, unfulfilledQuantity);

    return new TestAllocationDecision(
        allocatedQuantity, remainingAvailableQuantity, unfulfilledQuantity, status);
  }

  private static TestAllocationStatus determineStatus(
      TestAllocationQuantity allocatedQuantity, TestAllocationQuantity unfulfilledQuantity) {
    if (allocatedQuantity.isZero()) {
      return TestAllocationStatus.NOT_ALLOCATED;
    }

    if (unfulfilledQuantity.isZero()) {
      return TestAllocationStatus.FULLY_ALLOCATED;
    }

    return TestAllocationStatus.PARTIALLY_ALLOCATED;
  }
}
