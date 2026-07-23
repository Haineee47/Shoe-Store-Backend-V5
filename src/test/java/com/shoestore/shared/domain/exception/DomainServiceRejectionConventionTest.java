package com.shoestore.shared.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.shoestore.shared.domain.exception.fixture.TestAllocationRequest;
import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestInventoryAllocation;
import com.shoestore.shared.domain.exception.fixture.TestInventoryAllocationService;
import com.shoestore.shared.domain.exception.fixture.TestInventoryAvailability;
import com.shoestore.shared.domain.exception.fixture.TestInventoryErrorCode;
import org.junit.jupiter.api.Test;

class DomainServiceRejectionConventionTest {

  private final TestInventoryAllocationService service = new TestInventoryAllocationService();

  @Test
  void shouldAllocateInventoryWhenBusinessRulesAreSatisfied() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 3);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    TestInventoryAllocation allocation = service.allocate(request, inventory);

    assertThat(allocation.sku()).isEqualTo("SKU-001");
    assertThat(allocation.allocatedQuantity()).isEqualTo(3);
    assertThat(allocation.remainingQuantity()).isEqualTo(7);
  }

  @Test
  void shouldRejectNonPositiveAllocationQuantity() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 0);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    TestDomainException exception =
        catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode())
        .isSameAs(TestInventoryErrorCode.ALLOCATION_QUANTITY_MUST_BE_POSITIVE);
    assertThat(exception).hasMessage("Allocation quantity must be greater than zero");
  }

  @Test
  void shouldRejectAllocationFromNonSellableInventory() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 2);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, false);

    TestDomainException exception =
        catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode()).isSameAs(TestInventoryErrorCode.INVENTORY_NOT_SELLABLE);
    assertThat(exception).hasMessage("Inventory must be sellable before allocation");
  }

  @Test
  void shouldRejectAllocationWhenSkuDoesNotMatch() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-REQUESTED", 2);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-AVAILABLE", 10, true);

    TestDomainException exception =
        catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode()).isSameAs(TestInventoryErrorCode.INVENTORY_SKU_MISMATCH);
    assertThat(exception).hasMessage("Allocation request SKU must match inventory SKU");
  }

  @Test
  void shouldRejectAllocationWhenAvailableQuantityIsInsufficient() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 11);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    TestDomainException exception =
        catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.errorCode())
        .isSameAs(TestInventoryErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
    assertThat(exception).hasMessage("Requested quantity exceeds available inventory");
  }

  @Test
  void shouldNotMutateInputsWhenAllocationIsRejected() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 11);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    assertThat(request.sku()).isEqualTo("SKU-001");
    assertThat(request.requestedQuantity()).isEqualTo(11);

    assertThat(inventory.sku()).isEqualTo("SKU-001");
    assertThat(inventory.availableQuantity()).isEqualTo(10);
    assertThat(inventory.sellable()).isTrue();
  }

  @Test
  void shouldProduceSameResultForSameInputs() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 4);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    TestInventoryAllocation first = service.allocate(request, inventory);

    TestInventoryAllocation second = service.allocate(request, inventory);

    assertThat(second).isEqualTo(first);
  }

  @Test
  void shouldProduceSameRejectionForSameInvalidInputs() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 15);

    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    TestDomainException first =
        catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    TestDomainException second =
        catchThrowableOfType(() -> service.allocate(request, inventory), TestDomainException.class);

    assertThat(first).isNotNull();
    assertThat(second).isNotNull();

    assertThat(second.errorCode()).isSameAs(first.errorCode());

    assertThat(second.getMessage()).isEqualTo(first.getMessage());
  }

  @Test
  void shouldRejectNullRequestAsProgrammingContractViolation() {
    TestInventoryAvailability inventory = new TestInventoryAvailability("SKU-001", 10, true);

    org.assertj.core.api.Assertions.assertThatNullPointerException()
        .isThrownBy(() -> service.allocate(null, inventory))
        .withMessage("request must not be null");
  }

  @Test
  void shouldRejectNullInventoryAsProgrammingContractViolation() {
    TestAllocationRequest request = new TestAllocationRequest("SKU-001", 1);

    org.assertj.core.api.Assertions.assertThatNullPointerException()
        .isThrownBy(() -> service.allocate(request, null))
        .withMessage("inventory must not be null");
  }
}
