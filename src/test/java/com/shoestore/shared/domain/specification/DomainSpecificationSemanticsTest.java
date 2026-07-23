package com.shoestore.shared.domain.specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.specification.fixture.TestProductCandidate;
import com.shoestore.shared.domain.specification.fixture.TestProductHasAvailableStockSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductIsActiveSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductStatus;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DomainSpecificationSemanticsTest {

  private final Specification<TestProductCandidate> activeSpecification =
      new TestProductIsActiveSpecification();

  private final Specification<TestProductCandidate> availableStockSpecification =
      new TestProductHasAvailableStockSpecification();

  @Test
  void andShouldReturnTrueWhenBothSpecificationsAreSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 5);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void andShouldReturnFalseWhenLeftSpecificationIsNotSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 5);

    assertThat(specification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void andShouldReturnFalseWhenRightSpecificationIsNotSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 0);

    assertThat(specification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void andShouldReturnFalseWhenNeitherSpecificationIsSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 0);

    assertThat(specification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void orShouldReturnTrueWhenBothSpecificationsAreSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 5);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void orShouldReturnTrueWhenOnlyLeftSpecificationIsSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 0);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void orShouldReturnTrueWhenOnlyRightSpecificationIsSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 5);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void orShouldReturnFalseWhenNeitherSpecificationIsSatisfied() {
    Specification<TestProductCandidate> specification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 0);

    assertThat(specification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void notShouldInvertSatisfiedResult() {
    Specification<TestProductCandidate> inactiveSpecification = activeSpecification.not();

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 0);

    assertThat(inactiveSpecification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void notShouldInvertUnsatisfiedResult() {
    Specification<TestProductCandidate> inactiveSpecification = activeSpecification.not();

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 0);

    assertThat(inactiveSpecification.isSatisfiedBy(candidate)).isFalse();
  }

  @Test
  void doubleNegationShouldPreserveOriginalResult() {
    Specification<TestProductCandidate> doubleNegatedSpecification =
        activeSpecification.not().not();

    TestProductCandidate activeCandidate = candidate(TestProductStatus.ACTIVE, 0);

    TestProductCandidate inactiveCandidate = candidate(TestProductStatus.INACTIVE, 0);

    assertThat(doubleNegatedSpecification.isSatisfiedBy(activeCandidate))
        .isEqualTo(activeSpecification.isSatisfiedBy(activeCandidate));

    assertThat(doubleNegatedSpecification.isSatisfiedBy(inactiveCandidate))
        .isEqualTo(activeSpecification.isSatisfiedBy(inactiveCandidate));
  }

  @Test
  void andShouldShortCircuitWhenLeftSpecificationReturnsFalse() {
    AtomicInteger rightEvaluationCount = new AtomicInteger();

    Specification<TestProductCandidate> rightSpecification =
        candidate -> {
          rightEvaluationCount.incrementAndGet();
          return true;
        };

    Specification<TestProductCandidate> specification = activeSpecification.and(rightSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 5);

    assertThat(specification.isSatisfiedBy(candidate)).isFalse();

    assertThat(rightEvaluationCount.get()).isZero();
  }

  @Test
  void andShouldEvaluateRightSpecificationWhenLeftReturnsTrue() {
    AtomicInteger rightEvaluationCount = new AtomicInteger();

    Specification<TestProductCandidate> rightSpecification =
        candidate -> {
          rightEvaluationCount.incrementAndGet();
          return true;
        };

    Specification<TestProductCandidate> specification = activeSpecification.and(rightSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 5);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();

    assertThat(rightEvaluationCount.get()).isEqualTo(1);
  }

  @Test
  void orShouldShortCircuitWhenLeftSpecificationReturnsTrue() {
    AtomicInteger rightEvaluationCount = new AtomicInteger();

    Specification<TestProductCandidate> rightSpecification =
        candidate -> {
          rightEvaluationCount.incrementAndGet();
          return false;
        };

    Specification<TestProductCandidate> specification = activeSpecification.or(rightSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 0);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();

    assertThat(rightEvaluationCount.get()).isZero();
  }

  @Test
  void orShouldEvaluateRightSpecificationWhenLeftReturnsFalse() {
    AtomicInteger rightEvaluationCount = new AtomicInteger();

    Specification<TestProductCandidate> rightSpecification =
        candidate -> {
          rightEvaluationCount.incrementAndGet();
          return true;
        };

    Specification<TestProductCandidate> specification = activeSpecification.or(rightSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 0);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();

    assertThat(rightEvaluationCount.get()).isEqualTo(1);
  }

  @Test
  void composedAndSpecificationShouldRejectNullCandidate() {
    Specification<TestProductCandidate> specification =
        activeSpecification.and(availableStockSpecification);

    assertThatNullPointerException()
        .isThrownBy(() -> specification.isSatisfiedBy(null))
        .withMessage("candidate must not be null");
  }

  @Test
  void composedOrSpecificationShouldRejectNullCandidate() {
    Specification<TestProductCandidate> specification =
        activeSpecification.or(availableStockSpecification);

    assertThatNullPointerException()
        .isThrownBy(() -> specification.isSatisfiedBy(null))
        .withMessage("candidate must not be null");
  }

  @Test
  void negatedSpecificationShouldRejectNullCandidate() {
    Specification<TestProductCandidate> specification = activeSpecification.not();

    assertThatNullPointerException()
        .isThrownBy(() -> specification.isSatisfiedBy(null))
        .withMessage("candidate must not be null");
  }

  @Test
  void andCompositionShouldPreserveAssociativeBooleanResult() {
    Specification<TestProductCandidate> hasEvenQuantitySpecification =
        candidate -> candidate.availableQuantity() % 2 == 0;

    Specification<TestProductCandidate> leftAssociated =
        activeSpecification.and(availableStockSpecification).and(hasEvenQuantitySpecification);

    Specification<TestProductCandidate> rightAssociated =
        activeSpecification.and(availableStockSpecification.and(hasEvenQuantitySpecification));

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 4);

    assertThat(leftAssociated.isSatisfiedBy(candidate))
        .isEqualTo(rightAssociated.isSatisfiedBy(candidate));
  }

  @Test
  void orCompositionShouldPreserveAssociativeBooleanResult() {
    Specification<TestProductCandidate> hasEvenQuantitySpecification =
        candidate -> candidate.availableQuantity() % 2 == 0;

    Specification<TestProductCandidate> leftAssociated =
        activeSpecification.or(availableStockSpecification).or(hasEvenQuantitySpecification);

    Specification<TestProductCandidate> rightAssociated =
        activeSpecification.or(availableStockSpecification.or(hasEvenQuantitySpecification));

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 0);

    assertThat(leftAssociated.isSatisfiedBy(candidate))
        .isEqualTo(rightAssociated.isSatisfiedBy(candidate));
  }

  private static TestProductCandidate candidate(TestProductStatus status, int availableQuantity) {

    return new TestProductCandidate(status, availableQuantity);
  }
}
