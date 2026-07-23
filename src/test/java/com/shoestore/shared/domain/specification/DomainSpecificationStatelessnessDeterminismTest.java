package com.shoestore.shared.domain.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.specification.fixture.TestProductCandidate;
import com.shoestore.shared.domain.specification.fixture.TestProductHasAvailableStockSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductIsActiveSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductMinimumQuantitySpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductStatus;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class DomainSpecificationStatelessnessDeterminismTest {

  private static final int REPEATED_EVALUATION_COUNT = 100;

  private final Specification<TestProductCandidate> activeSpecification =
      new TestProductIsActiveSpecification();

  private final Specification<TestProductCandidate> availableStockSpecification =
      new TestProductHasAvailableStockSpecification();

  @Test
  void fixtureSpecificationsShouldDeclareNoInstanceFields() {
    assertHasNoInstanceFields(TestProductIsActiveSpecification.class);

    assertHasNoInstanceFields(TestProductHasAvailableStockSpecification.class);
  }

  @Test
  void fixtureSpecificationsShouldDeclareNoMutableStaticFields() {
    assertHasNoMutableStaticFields(TestProductIsActiveSpecification.class);

    assertHasNoMutableStaticFields(TestProductHasAvailableStockSpecification.class);
  }

  @Test
  void fixtureSpecificationsShouldBeFinal() {
    assertThat(Modifier.isFinal(TestProductIsActiveSpecification.class.getModifiers())).isTrue();

    assertThat(Modifier.isFinal(TestProductHasAvailableStockSpecification.class.getModifiers()))
        .isTrue();
  }

  @Test
  void repeatedEvaluationShouldReturnSameResultForActiveSpecification() {
    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 3);

    boolean expected = activeSpecification.isSatisfiedBy(candidate);

    for (int index = 0; index < REPEATED_EVALUATION_COUNT; index++) {

      assertThat(activeSpecification.isSatisfiedBy(candidate)).isEqualTo(expected);
    }
  }

  @Test
  void repeatedEvaluationShouldReturnSameResultForStockSpecification() {
    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 3);

    boolean expected = availableStockSpecification.isSatisfiedBy(candidate);

    for (int index = 0; index < REPEATED_EVALUATION_COUNT; index++) {

      assertThat(availableStockSpecification.isSatisfiedBy(candidate)).isEqualTo(expected);
    }
  }

  @Test
  void repeatedEvaluationShouldReturnSameResultForAndComposition() {
    Specification<TestProductCandidate> composedSpecification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 3);

    boolean expected = composedSpecification.isSatisfiedBy(candidate);

    for (int index = 0; index < REPEATED_EVALUATION_COUNT; index++) {

      assertThat(composedSpecification.isSatisfiedBy(candidate)).isEqualTo(expected);
    }
  }

  @Test
  void repeatedEvaluationShouldReturnSameResultForOrComposition() {
    Specification<TestProductCandidate> composedSpecification =
        activeSpecification.or(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 3);

    boolean expected = composedSpecification.isSatisfiedBy(candidate);

    for (int index = 0; index < REPEATED_EVALUATION_COUNT; index++) {

      assertThat(composedSpecification.isSatisfiedBy(candidate)).isEqualTo(expected);
    }
  }

  @Test
  void repeatedEvaluationShouldReturnSameResultForNegation() {
    Specification<TestProductCandidate> negatedSpecification = activeSpecification.not();

    TestProductCandidate candidate = candidate(TestProductStatus.INACTIVE, 0);

    boolean expected = negatedSpecification.isSatisfiedBy(candidate);

    for (int index = 0; index < REPEATED_EVALUATION_COUNT; index++) {

      assertThat(negatedSpecification.isSatisfiedBy(candidate)).isEqualTo(expected);
    }
  }

  @Test
  void evaluationShouldNotMutateCandidate() {
    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 7);

    TestProductStatus statusBefore = candidate.status();

    int quantityBefore = candidate.availableQuantity();

    activeSpecification.and(availableStockSpecification).isSatisfiedBy(candidate);

    assertThat(candidate.status()).isEqualTo(statusBefore);

    assertThat(candidate.availableQuantity()).isEqualTo(quantityBefore);
  }

  @Test
  void evaluationOrderShouldNotChangeIndependentSpecificationResults() {
    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 5);

    boolean activeBefore = activeSpecification.isSatisfiedBy(candidate);

    boolean stockResult = availableStockSpecification.isSatisfiedBy(candidate);

    boolean activeAfter = activeSpecification.isSatisfiedBy(candidate);

    assertThat(activeBefore).isTrue();
    assertThat(stockResult).isTrue();

    assertThat(activeAfter).isEqualTo(activeBefore);
  }

  @Test
  void evaluatingOneCandidateShouldNotAffectAnotherCandidate() {
    TestProductCandidate activeCandidate = candidate(TestProductStatus.ACTIVE, 1);

    TestProductCandidate inactiveCandidate = candidate(TestProductStatus.INACTIVE, 1);

    boolean firstActiveResult = activeSpecification.isSatisfiedBy(activeCandidate);

    boolean inactiveResult = activeSpecification.isSatisfiedBy(inactiveCandidate);

    boolean secondActiveResult = activeSpecification.isSatisfiedBy(activeCandidate);

    assertThat(firstActiveResult).isTrue();
    assertThat(inactiveResult).isFalse();

    assertThat(secondActiveResult).isEqualTo(firstActiveResult);
  }

  @Test
  void newInstancesShouldProduceEquivalentResults() {
    Specification<TestProductCandidate> firstSpecification = new TestProductIsActiveSpecification();

    Specification<TestProductCandidate> secondSpecification =
        new TestProductIsActiveSpecification();

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 2);

    assertThat(firstSpecification.isSatisfiedBy(candidate))
        .isEqualTo(secondSpecification.isSatisfiedBy(candidate));
  }

  @Test
  void concurrentEvaluationShouldProduceConsistentResults() throws Exception {

    Specification<TestProductCandidate> composedSpecification =
        activeSpecification.and(availableStockSpecification);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 10);

    try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {

      List<Callable<Boolean>> tasks =
          List.of(
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate),
              () -> composedSpecification.isSatisfiedBy(candidate));

      List<Future<Boolean>> results = executorService.invokeAll(tasks);

      for (Future<Boolean> result : results) {
        assertThat(result.get()).isTrue();
      }
    }
  }

  @Test
  void deterministicSpecificationShouldNotDependOnInvocationCount() {
    Specification<TestProductCandidate> specification =
        candidate -> candidate.status() == TestProductStatus.ACTIVE;

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 0);

    boolean firstResult = specification.isSatisfiedBy(candidate);

    boolean secondResult = specification.isSatisfiedBy(candidate);

    boolean thirdResult = specification.isSatisfiedBy(candidate);

    assertThat(firstResult).isTrue();
    assertThat(secondResult).isTrue();
    assertThat(thirdResult).isTrue();
  }

  @Test
  void immutableConfiguredSpecificationShouldRemainDeterministic() {
    Specification<TestProductCandidate> specification =
        new TestProductMinimumQuantitySpecification(5);

    TestProductCandidate candidate = candidate(TestProductStatus.ACTIVE, 7);

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();

    assertThat(specification.isSatisfiedBy(candidate)).isTrue();
  }

  private static void assertHasNoInstanceFields(Class<?> specificationType) {

    List<Field> instanceFields =
        Arrays.stream(specificationType.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .toList();

    assertThat(instanceFields)
        .as("%s must not declare instance fields", specificationType.getSimpleName())
        .isEmpty();
  }

  private static void assertHasNoMutableStaticFields(Class<?> specificationType) {

    List<Field> mutableStaticFields =
        Arrays.stream(specificationType.getDeclaredFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()))
            .filter(field -> !Modifier.isFinal(field.getModifiers()))
            .toList();

    assertThat(mutableStaticFields)
        .as("%s must not declare mutable static fields", specificationType.getSimpleName())
        .isEmpty();
  }

  private static TestProductCandidate candidate(TestProductStatus status, int availableQuantity) {

    return new TestProductCandidate(status, availableQuantity);
  }

  private static void assertAllInstanceFieldsAreFinal(Class<?> specificationType) {

    List<Field> nonFinalInstanceFields =
        Arrays.stream(specificationType.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .filter(field -> !Modifier.isFinal(field.getModifiers()))
            .toList();

    assertThat(nonFinalInstanceFields)
        .as("%s must not declare mutable instance fields", specificationType.getSimpleName())
        .isEmpty();
  }
}
