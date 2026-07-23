package com.shoestore.shared.domain.exception.fixture;

/**
 * Test-only module-specific domain error codes.
 *
 * <p>The enum simulates error-code ownership by a business module without introducing production
 * business rules.
 */
public enum TestOrderErrorCode implements TestDomainErrorCode {
  ORDER_HAS_NO_ITEMS,

  ORDER_ALREADY_CONFIRMED,

  ORDER_CANNOT_BE_MODIFIED,

  ORDER_CANNOT_BE_CONFIRMED,

  ORDER_CANNOT_BE_CANCELLED,

  ORDER_CANNOT_BE_COMPLETED,

  ORDER_ITEM_QUANTITY_MUST_BE_POSITIVE
}
