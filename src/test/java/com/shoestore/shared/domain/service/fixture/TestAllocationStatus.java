package com.shoestore.shared.domain.service.fixture;

/**
 * Describes the outcome of an allocation decision.
 */
public enum TestAllocationStatus {

    /**
     * The complete requested quantity was allocated.
     */
    FULLY_ALLOCATED,

    /**
     * Only part of the requested quantity was allocated.
     */
    PARTIALLY_ALLOCATED,

    /**
     * No quantity could be allocated.
     */
    NOT_ALLOCATED
}
