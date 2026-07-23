package com.shoestore.shared.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.policy.fixture.StandardTestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestCancellationContext;
import com.shoestore.shared.domain.policy.fixture.TestCancellationDecision;
import com.shoestore.shared.domain.policy.fixture.TestCancellationOrderStatus;
import com.shoestore.shared.domain.policy.fixture.TestFulfillmentStatus;
import com.shoestore.shared.domain.policy.fixture.TestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestPaymentStatus;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class DomainPolicyStatelessnessDeterminismTest {

  @Test
  void policyImplementationShouldDeclareNoInstanceFields() {
    Field[] instanceFields =
        java.util.Arrays.stream(StandardTestOrderCancellationPolicy.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .toArray(Field[]::new);

    assertThat(instanceFields).isEmpty();
  }

  @Test
  void policyImplementationShouldDeclareNoMutableStaticFields() {
    Field[] mutableStaticFields =
        java.util.Arrays.stream(StandardTestOrderCancellationPolicy.class.getDeclaredFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()))
            .filter(field -> !Modifier.isFinal(field.getModifiers()))
            .toArray(Field[]::new);

    assertThat(mutableStaticFields).isEmpty();
  }

  @Test
  void repeatedEvaluationShouldAlwaysReturnSameDecision() {
    TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.AUTHORIZED,
            TestFulfillmentStatus.PROCESSING);

    TestCancellationDecision expected = TestCancellationDecision.ALLOWED;

    for (int invocation = 0; invocation < 100; invocation++) {
      assertThat(policy.evaluate(context)).isEqualTo(expected);
    }
  }

  @Test
  void separatePolicyInstancesShouldReturnSameDecision() {
    TestOrderCancellationPolicy firstPolicy = new StandardTestOrderCancellationPolicy();

    TestOrderCancellationPolicy secondPolicy = new StandardTestOrderCancellationPolicy();

    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.PROCESSING);

    assertThat(firstPolicy.evaluate(context))
        .isEqualTo(secondPolicy.evaluate(context))
        .isEqualTo(TestCancellationDecision.REJECTED_PAYMENT_SETTLED);
  }

  @Test
  void priorAllowedEvaluationShouldNotAffectRejectedEvaluation() {
    TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

    TestCancellationContext allowedContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.DRAFT,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.NOT_STARTED);

    TestCancellationContext rejectedContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.SHIPPED);

    assertThat(policy.evaluate(allowedContext)).isEqualTo(TestCancellationDecision.ALLOWED);

    assertThat(policy.evaluate(rejectedContext))
        .isEqualTo(TestCancellationDecision.REJECTED_ALREADY_SHIPPED);
  }

  @Test
  void priorRejectedEvaluationShouldNotAffectAllowedEvaluation() {
    TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

    TestCancellationContext rejectedContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.CANCELLED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.SHIPPED);

    TestCancellationContext allowedContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.AUTHORIZED,
            TestFulfillmentStatus.PROCESSING);

    assertThat(policy.evaluate(rejectedContext))
        .isEqualTo(TestCancellationDecision.REJECTED_ALREADY_CANCELLED);

    assertThat(policy.evaluate(allowedContext)).isEqualTo(TestCancellationDecision.ALLOWED);
  }

  @Test
  void evaluationOrderShouldNotChangeFinalDecision() {
    TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

    TestCancellationContext targetContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.PROCESSING);

    List<TestCancellationContext> unrelatedContexts =
        new ArrayList<>(
            List.of(
                new TestCancellationContext(
                    TestCancellationOrderStatus.DRAFT,
                    TestPaymentStatus.PENDING,
                    TestFulfillmentStatus.NOT_STARTED),
                new TestCancellationContext(
                    TestCancellationOrderStatus.CANCELLED,
                    TestPaymentStatus.AUTHORIZED,
                    TestFulfillmentStatus.PROCESSING),
                new TestCancellationContext(
                    TestCancellationOrderStatus.CONFIRMED,
                    TestPaymentStatus.PENDING,
                    TestFulfillmentStatus.SHIPPED)));

    TestCancellationDecision decisionBefore = policy.evaluate(targetContext);

    Collections.reverse(unrelatedContexts);

    unrelatedContexts.forEach(policy::evaluate);

    TestCancellationDecision decisionAfter = policy.evaluate(targetContext);

    assertThat(decisionAfter)
        .isEqualTo(decisionBefore)
        .isEqualTo(TestCancellationDecision.REJECTED_PAYMENT_SETTLED);
  }

  @Test
  void allPolicyInstancesShouldProduceSameDecisionMatrix() {
    TestOrderCancellationPolicy firstPolicy = new StandardTestOrderCancellationPolicy();

    TestOrderCancellationPolicy secondPolicy = new StandardTestOrderCancellationPolicy();

    for (TestCancellationOrderStatus orderStatus : TestCancellationOrderStatus.values()) {

      for (TestPaymentStatus paymentStatus : TestPaymentStatus.values()) {

        for (TestFulfillmentStatus fulfillmentStatus : TestFulfillmentStatus.values()) {

          TestCancellationContext context =
              new TestCancellationContext(orderStatus, paymentStatus, fulfillmentStatus);

          assertThat(firstPolicy.evaluate(context)).isEqualTo(secondPolicy.evaluate(context));
        }
      }
    }
  }

  @Test
  void concurrentEvaluationsShouldReturnSameDecision() throws Exception {

    TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.SHIPPED);

    ExecutorService executor = Executors.newFixedThreadPool(8);

    try {
      Callable<TestCancellationDecision> task = () -> policy.evaluate(context);

      List<Callable<TestCancellationDecision>> tasks = Collections.nCopies(100, task);

      List<Future<TestCancellationDecision>> futures = executor.invokeAll(tasks);

      for (Future<TestCancellationDecision> future : futures) {
        assertThat(future.get()).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_SHIPPED);
      }
    } finally {
      executor.shutdownNow();
    }
  }
}
