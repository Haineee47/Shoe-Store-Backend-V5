package com.shoestore.shared.domain.exception.fixture;

/**
 * Test-only inventory error codes used to verify rejection conventions
 * for stateless domain services.
 */
public enum TestInventoryErrorCode implements TestDomainErrorCode {

    ALLOCATION_QUANTITY_MUST_BE_POSITIVE,

    INVENTORY_NOT_SELLABLE,

    INVENTORY_SKU_MISMATCH,

    INSUFFICIENT_AVAILABLE_QUANTITY
}
