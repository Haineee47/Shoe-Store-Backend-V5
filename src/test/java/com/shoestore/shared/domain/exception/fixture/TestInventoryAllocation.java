package com.shoestore.shared.domain.exception.fixture;

import java.util.Objects;

/**
 * Immutable test-only result returned by a successful allocation decision.
 */
public record TestInventoryAllocation(
        String sku,
        int allocatedQuantity,
        int remainingQuantity
) {

    public TestInventoryAllocation {
        Objects.requireNonNull(sku, "sku must not be null");

        if (sku.isBlank()) {
            throw new IllegalArgumentException(
                    "sku must not be blank"
            );
        }

        if (allocatedQuantity <= 0) {
            throw new IllegalArgumentException(
                    "allocatedQuantity must be positive"
            );
        }

        if (remainingQuantity < 0) {
            throw new IllegalArgumentException(
                    "remainingQuantity must not be negative"
            );
        }
    }
}
