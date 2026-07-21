package com.shoestore.shared.domain.exception.fixture;

import java.util.Objects;

/**
 * Immutable test-only representation of available inventory.
 */
public record TestInventoryAvailability(
        String sku,
        int availableQuantity,
        boolean sellable
) {

    public TestInventoryAvailability {
        Objects.requireNonNull(sku, "sku must not be null");

        if (sku.isBlank()) {
            throw new IllegalArgumentException(
                    "sku must not be blank"
            );
        }

        if (availableQuantity < 0) {
            throw new IllegalArgumentException(
                    "availableQuantity must not be negative"
            );
        }
    }
}
