package com.shoestore.shared.domain.specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.specification.fixture.TestProductCandidate;
import com.shoestore.shared.domain.specification.fixture.TestProductHasAvailableStockSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductIsActiveSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductStatus;
import org.junit.jupiter.api.Test;

class DomainSpecificationFixtureTest {

  private final Specification<TestProductCandidate> activeSpecification =
      new TestProductIsActiveSpecification();

  private final Specification<TestProductCandidate> availableStockSpecification =
      new TestProductHasAvailableStockSpecification();

  @Test
  void shouldReturnTrueWhenProductIsActive() {
    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.ACTIVE, 0);

    boolean satisfied = activeSpecification.isSatisfiedBy(candidate);

    assertThat(satisfied).isTrue();
  }

  @Test
  void shouldReturnFalseWhenProductIsInactive() {
    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.INACTIVE, 10);

    boolean satisfied = activeSpecification.isSatisfiedBy(candidate);

    assertThat(satisfied).isFalse();
  }

  @Test
  void shouldReturnTrueWhenProductHasAvailableStock() {
    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.INACTIVE, 1);

    boolean satisfied = availableStockSpecification.isSatisfiedBy(candidate);

    assertThat(satisfied).isTrue();
  }

  @Test
  void shouldReturnFalseWhenProductHasNoAvailableStock() {
    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.ACTIVE, 0);

    boolean satisfied = availableStockSpecification.isSatisfiedBy(candidate);

    assertThat(satisfied).isFalse();
  }

  @Test
  void andShouldRequireBothSpecificationsToBeSatisfied() {
    Specification<TestProductCandidate> sellableSpecification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.ACTIVE, 5);

    assertThat(sellableSpecification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void andShouldReturnFalseWhenOneSpecificationIsNotSatisfied() {
    Specification<TestProductCandidate> sellableSpecification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.ACTIVE, 0);

    assertThat(sellableSpecification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void orShouldRequireAtLeastOneSpecificationToBeSatisfied() {
    Specification<TestProductCandidate> activeOrAvailableSpecification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.INACTIVE, 3);

    assertThat(activeOrAvailableSpecification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void orShouldReturnFalseWhenNoSpecificationIsSatisfied() {
    Specification<TestProductCandidate> activeOrAvailableSpecification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.INACTIVE, 0);

    assertThat(activeOrAvailableSpecification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void notShouldNegateSpecificationResult() {
    Specification<TestProductCandidate> inactiveSpecification = activeSpecification.not();

    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.INACTIVE, 0);

    assertThat(inactiveSpecification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void concreteSpecificationShouldRejectNullCandidate() {
    assertThatNullPointerException()
        .isThrownBy(() -> activeSpecification.isSatisfiedBy(null))
        .withMessage("candidate must not be null");
  }

  @Test
  void andShouldRejectNullSpecification() {
    assertThatNullPointerException()
        .isThrownBy(() -> activeSpecification.and(null))
        .withMessage("other specification must not be null");
  }

  @Test
  void orShouldRejectNullSpecification() {
    assertThatNullPointerException()
        .isThrownBy(() -> activeSpecification.or(null))
        .withMessage("other specification must not be null");
  }
}
