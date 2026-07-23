package com.shoestore.shared.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.shoestore.shared.domain.policy.fixture.StandardTestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestCancellationContext;
import com.shoestore.shared.domain.policy.fixture.TestCancellationDecision;
import com.shoestore.shared.domain.policy.fixture.TestCancellationOrderStatus;
import com.shoestore.shared.domain.policy.fixture.TestFulfillmentStatus;
import com.shoestore.shared.domain.policy.fixture.TestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestPaymentStatus;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DomainPolicyDecisionSemanticsTest {

  private final TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

  @ParameterizedTest
  @MethodSource("allowedCancellationContexts")
  void shouldAllowCancellationWhenNoRejectionRuleMatches(TestCancellationContext context) {

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.ALLOWED);
  }

  @ParameterizedTest
  @MethodSource("alreadyCancelledContexts")
  void shouldRejectCancelledOrdersRegardlessOfOtherState(TestCancellationContext context) {

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_CANCELLED);
  }

  @ParameterizedTest
  @MethodSource("shippedOrderContexts")
  void shouldRejectShippedOrdersWhenOrderIsNotAlreadyCancelled(TestCancellationContext context) {

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_SHIPPED);
  }

  @ParameterizedTest
  @MethodSource("settledPaymentContexts")
  void shouldRejectSettledPaymentsWhenHigherPriorityRulesDoNotMatch(
      TestCancellationContext context) {

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_PAYMENT_SETTLED);
  }

  @Test
  void shouldApplyAlreadyCancelledRuleBeforeShippedAndSettledRules() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CANCELLED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.SHIPPED);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_CANCELLED);
  }

  @Test
  void shouldApplyShippedRuleBeforeSettledPaymentRule() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.SHIPPED);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_SHIPPED);
  }

  @Test
  void shouldRepresentNormalBusinessRejectionAsDecisionInsteadOfException() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.SHIPPED);

    assertThatCode(() -> policy.evaluate(context)).doesNotThrowAnyException();

    assertThat(policy.evaluate(context))
        .isEqualTo(TestCancellationDecision.REJECTED_ALREADY_SHIPPED);
  }

  @Test
  void shouldNeverReturnNullForValidContext() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.DRAFT,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.NOT_STARTED);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isNotNull();
  }

  @Test
  void shouldProduceSameDecisionAfterEvaluatingDifferentContext() {
    TestCancellationContext originalContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.AUTHORIZED,
            TestFulfillmentStatus.PROCESSING);

    TestCancellationContext unrelatedContext =
        new TestCancellationContext(
            TestCancellationOrderStatus.CANCELLED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.SHIPPED);

    TestCancellationDecision firstDecision = policy.evaluate(originalContext);

    policy.evaluate(unrelatedContext);

    TestCancellationDecision secondDecision = policy.evaluate(originalContext);

    assertThat(secondDecision).isEqualTo(firstDecision).isEqualTo(TestCancellationDecision.ALLOWED);
  }

  @Test
  void shouldNotModifyEvaluationContext() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.AUTHORIZED,
            TestFulfillmentStatus.PROCESSING);

    policy.evaluate(context);

    assertThat(context.orderStatus()).isEqualTo(TestCancellationOrderStatus.CONFIRMED);

    assertThat(context.paymentStatus()).isEqualTo(TestPaymentStatus.AUTHORIZED);

    assertThat(context.fulfillmentStatus()).isEqualTo(TestFulfillmentStatus.PROCESSING);
  }

  private static Stream<Arguments> allowedCancellationContexts() {
    return Stream.of(
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.DRAFT,
                TestPaymentStatus.PENDING,
                TestFulfillmentStatus.NOT_STARTED)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.DRAFT,
                TestPaymentStatus.AUTHORIZED,
                TestFulfillmentStatus.PROCESSING)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CONFIRMED,
                TestPaymentStatus.PENDING,
                TestFulfillmentStatus.NOT_STARTED)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CONFIRMED,
                TestPaymentStatus.AUTHORIZED,
                TestFulfillmentStatus.PROCESSING)));
  }

  private static Stream<Arguments> alreadyCancelledContexts() {
    return Stream.of(
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CANCELLED,
                TestPaymentStatus.PENDING,
                TestFulfillmentStatus.NOT_STARTED)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CANCELLED,
                TestPaymentStatus.AUTHORIZED,
                TestFulfillmentStatus.PROCESSING)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CANCELLED,
                TestPaymentStatus.SETTLED,
                TestFulfillmentStatus.SHIPPED)));
  }

  private static Stream<Arguments> shippedOrderContexts() {
    return Stream.of(
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.DRAFT,
                TestPaymentStatus.PENDING,
                TestFulfillmentStatus.SHIPPED)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CONFIRMED,
                TestPaymentStatus.AUTHORIZED,
                TestFulfillmentStatus.SHIPPED)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CONFIRMED,
                TestPaymentStatus.SETTLED,
                TestFulfillmentStatus.SHIPPED)));
  }

  private static Stream<Arguments> settledPaymentContexts() {
    return Stream.of(
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.DRAFT,
                TestPaymentStatus.SETTLED,
                TestFulfillmentStatus.NOT_STARTED)),
        Arguments.of(
            new TestCancellationContext(
                TestCancellationOrderStatus.CONFIRMED,
                TestPaymentStatus.SETTLED,
                TestFulfillmentStatus.PROCESSING)));
  }
}
