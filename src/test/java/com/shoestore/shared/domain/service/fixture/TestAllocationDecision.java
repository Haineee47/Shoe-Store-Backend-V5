package com.shoestore.shared.domain.service.fixture;

import java.util.Objects;

/**
 * Immutable result returned by {@link TestAllocationPolicy}.
 */
public record TestAllocationDecision(
        TestAllocationQuantity allocatedQuantity,
        TestAllocationQuantity remainingAvailableQuantity,
        TestAllocationQuantity unfulfilledQuantity,
        TestAllocationStatus status
) {

    public TestAllocationDecision {
        Objects.requireNonNull(
                allocatedQuantity,
                "allocatedQuantity must not be null"
        );

        Objects.requireNonNull(
                remainingAvailableQuantity,
                "remainingAvailableQuantity must not be null"
        );

        Objects.requireNonNull(
                unfulfilledQuantity,
                "unfulfilledQuantity must not be null"
        );

        Objects.requireNonNull(
                status,
                "status must not be null"
        );

        validateStatusConsistency(
                allocatedQuantity,
                unfulfilledQuantity,
                status
        );
    }

    private static void validateStatusConsistency(
        TestAllocationQuantity allocatedQuantity,
        TestAllocationQuantity unfulfilledQuantity,
        TestAllocationStatus status
    ) {
        switch (status) {
            case FULLY_ALLOCATED -> {
                if (allocatedQuantity.isZero()) {
                    throw new IllegalArgumentException(
                            "fully allocated decision must have allocated quantity"
                    );
                }

                if (!unfulfilledQuantity.isZero()) {
                    throw new IllegalArgumentException(
                            "fully allocated decision must have zero unfulfilled quantity"
                    );
                }
            }

            case PARTIALLY_ALLOCATED -> {
                if (allocatedQuantity.isZero()
                        || unfulfilledQuantity.isZero()) {
                    throw new IllegalArgumentException(
                            "partially allocated decision must have allocated and unfulfilled quantities"
                    );
                }
            }

            case NOT_ALLOCATED -> {
                if (!allocatedQuantity.isZero()) {
                    throw new IllegalArgumentException(
                            "not allocated decision must have zero allocated quantity"
                    );
                }

                if (unfulfilledQuantity.isZero()) {
                    throw new IllegalArgumentException(
                            "not allocated decision must have unfulfilled quantity"
                    );
                }
            }
        }
    }

    public boolean isFullyAllocated() {
        return status == TestAllocationStatus.FULLY_ALLOCATED;
    }


}
