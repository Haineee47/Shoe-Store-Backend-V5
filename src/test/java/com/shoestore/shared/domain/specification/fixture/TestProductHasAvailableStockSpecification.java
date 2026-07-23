package com.shoestore.shared.domain.specification.fixture;

import com.shoestore.shared.domain.specification.Specification;
import java.util.Objects;

/** Test-only specification that determines whether a product candidate has available stock. */
public final class TestProductHasAvailableStockSpecification
    implements Specification<TestProductCandidate> {

  @Override
  public boolean isSatisfiedBy(TestProductCandidate candidate) {

    Objects.requireNonNull(candidate, "candidate must not be null");

    return candidate.availableQuantity() > 0;
  }
}
