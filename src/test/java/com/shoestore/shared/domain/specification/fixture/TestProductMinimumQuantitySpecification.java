package com.shoestore.shared.domain.specification.fixture;

import com.shoestore.shared.domain.specification.Specification;
import java.util.Objects;

/** Test-only immutable configured Specification. */
public final class TestProductMinimumQuantitySpecification
    implements Specification<TestProductCandidate> {

  private final int minimumQuantity;

  public TestProductMinimumQuantitySpecification(int minimumQuantity) {

    if (minimumQuantity < 0) {
      throw new IllegalArgumentException("minimumQuantity must not be negative");
    }

    this.minimumQuantity = minimumQuantity;
  }

  @Override
  public boolean isSatisfiedBy(TestProductCandidate candidate) {

    Objects.requireNonNull(candidate, "candidate must not be null");

    return candidate.availableQuantity() >= minimumQuantity;
  }
}
