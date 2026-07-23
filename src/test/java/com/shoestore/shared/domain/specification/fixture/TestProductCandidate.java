package com.shoestore.shared.domain.specification.fixture;

import java.util.Objects;

/**
 * Immutable test-only candidate evaluated by Domain Specifications.
 *
 * @param status the product business status
 * @param availableQuantity the quantity currently available
 */
public record TestProductCandidate(TestProductStatus status, int availableQuantity) {

  public TestProductCandidate {
    Objects.requireNonNull(status, "status must not be null");

    if (availableQuantity < 0) {
      throw new IllegalArgumentException("availableQuantity must not be negative");
    }
  }
}
