package com.shoestore.shared.domain.exception.fixture;

import java.util.Objects;

/**
 * Test-only stateless domain service used to verify business-rejection
 * conventions across multiple domain values.
 *
 * <p>The service has no repository, framework, persistence, clock,
 * random generator, logger, or mutable internal state.</p>
 */
public final class TestInventoryAllocationService {

    public TestInventoryAllocation allocate(
            TestAllocationRequest request,
            TestInventoryAvailability inventory
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(inventory, "inventory must not be null");

        if (request.requestedQuantity() <= 0) {
            throw new TestDomainException(
                    TestInventoryErrorCode.ALLOCATION_QUANTITY_MUST_BE_POSITIVE,
                    "Allocation quantity must be greater than zero"
            );
        }

        if (!inventory.sellable()) {
            throw new TestDomainException(
                    TestInventoryErrorCode.INVENTORY_NOT_SELLABLE,
                    "Inventory must be sellable before allocation"
            );
        }

        if (!inventory.sku().equals(request.sku())) {
            throw new TestDomainException(
                    TestInventoryErrorCode.INVENTORY_SKU_MISMATCH,
                    "Allocation request SKU must match inventory SKU"
            );
        }

        if (request.requestedQuantity() > inventory.availableQuantity()) {
            throw new TestDomainException(
                    TestInventoryErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY,
                    "Requested quantity exceeds available inventory"
            );
        }

        return new TestInventoryAllocation(
                request.sku(),
                request.requestedQuantity(),
                inventory.availableQuantity() - request.requestedQuantity()
        );
    }
}
