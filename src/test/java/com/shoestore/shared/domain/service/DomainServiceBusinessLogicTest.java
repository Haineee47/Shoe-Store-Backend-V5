package com.shoestore.shared.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.service.fixture.TestAllocationDecision;
import com.shoestore.shared.domain.service.fixture.TestAllocationPolicy;
import com.shoestore.shared.domain.service.fixture.TestAllocationQuantity;
import com.shoestore.shared.domain.service.fixture.TestAllocationStatus;
import org.junit.jupiter.api.Test;

class DomainServiceBusinessLogicTest {

  private final TestAllocationPolicy allocationPolicy = new TestAllocationPolicy();

  @Test
  void shouldFullyAllocateWhenAvailableQuantityExceedsRequestedQuantity() {
    TestAllocationQuantity requested = new TestAllocationQuantity(5);

    TestAllocationQuantity available = new TestAllocationQuantity(10);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    assertThat(decision.allocatedQuantity()).isEqualTo(new TestAllocationQuantity(5));

    assertThat(decision.remainingAvailableQuantity()).isEqualTo(new TestAllocationQuantity(5));

    assertThat(decision.unfulfilledQuantity()).isEqualTo(new TestAllocationQuantity(0));

    assertThat(decision.status()).isEqualTo(TestAllocationStatus.FULLY_ALLOCATED);

    assertThat(decision.isFullyAllocated()).isTrue();
  }

  @Test
  void shouldFullyAllocateWhenAvailableQuantityEqualsRequestedQuantity() {
    TestAllocationQuantity requested = new TestAllocationQuantity(5);

    TestAllocationQuantity available = new TestAllocationQuantity(5);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    assertThat(decision.allocatedQuantity()).isEqualTo(new TestAllocationQuantity(5));

    assertThat(decision.remainingAvailableQuantity()).isEqualTo(new TestAllocationQuantity(0));

    assertThat(decision.unfulfilledQuantity()).isEqualTo(new TestAllocationQuantity(0));

    assertThat(decision.status()).isEqualTo(TestAllocationStatus.FULLY_ALLOCATED);
  }

  @Test
  void shouldPartiallyAllocateWhenAvailableQuantityIsLessThanRequestedQuantity() {
    TestAllocationQuantity requested = new TestAllocationQuantity(10);

    TestAllocationQuantity available = new TestAllocationQuantity(4);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    assertThat(decision.allocatedQuantity()).isEqualTo(new TestAllocationQuantity(4));

    assertThat(decision.remainingAvailableQuantity()).isEqualTo(new TestAllocationQuantity(0));

    assertThat(decision.unfulfilledQuantity()).isEqualTo(new TestAllocationQuantity(6));

    assertThat(decision.status()).isEqualTo(TestAllocationStatus.PARTIALLY_ALLOCATED);

    assertThat(decision.isFullyAllocated()).isFalse();
  }

  @Test
  void shouldNotAllocateWhenNoQuantityIsAvailable() {
    TestAllocationQuantity requested = new TestAllocationQuantity(5);

    TestAllocationQuantity available = new TestAllocationQuantity(0);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    assertThat(decision.allocatedQuantity()).isEqualTo(new TestAllocationQuantity(0));

    assertThat(decision.remainingAvailableQuantity()).isEqualTo(new TestAllocationQuantity(0));

    assertThat(decision.unfulfilledQuantity()).isEqualTo(new TestAllocationQuantity(5));

    assertThat(decision.status()).isEqualTo(TestAllocationStatus.NOT_ALLOCATED);

    assertThat(decision.isFullyAllocated()).isFalse();
  }

  @Test
  void shouldPreserveRequestedQuantityConservation() {
    TestAllocationQuantity requested = new TestAllocationQuantity(13);

    TestAllocationQuantity available = new TestAllocationQuantity(8);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    int reconstructedRequested =
        Math.addExact(decision.allocatedQuantity().value(), decision.unfulfilledQuantity().value());

    assertThat(reconstructedRequested).isEqualTo(requested.value());
  }

  @Test
  void shouldPreserveAvailableQuantityConservation() {
    TestAllocationQuantity requested = new TestAllocationQuantity(6);

    TestAllocationQuantity available = new TestAllocationQuantity(11);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    int reconstructedAvailable =
        Math.addExact(
            decision.allocatedQuantity().value(), decision.remainingAvailableQuantity().value());

    assertThat(reconstructedAvailable).isEqualTo(available.value());
  }

  @Test
  void shouldNeverAllocateMoreThanRequestedQuantity() {
    TestAllocationQuantity requested = new TestAllocationQuantity(7);

    TestAllocationQuantity available = new TestAllocationQuantity(20);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    assertThat(decision.allocatedQuantity().value()).isLessThanOrEqualTo(requested.value());
  }

  @Test
  void shouldNeverAllocateMoreThanAvailableQuantity() {
    TestAllocationQuantity requested = new TestAllocationQuantity(20);

    TestAllocationQuantity available = new TestAllocationQuantity(7);

    TestAllocationDecision decision = allocationPolicy.allocate(requested, available);

    assertThat(decision.allocatedQuantity().value()).isLessThanOrEqualTo(available.value());
  }
}
