package com.shoestore.shared.domain.exception.fixture;

import java.util.Objects;

/**
 * Immutable test-only input to the inventory allocation domain service.
 */
public record TestAllocationRequest(
        String sku,
        int requestedQuantity
) {

    public TestAllocationRequest {
        Objects.requireNonNull(sku, "sku must not be null");

        if (sku.isBlank()) {
            throw new IllegalArgumentException(
                    "sku must not be blank"
            );
        }
    }
}
