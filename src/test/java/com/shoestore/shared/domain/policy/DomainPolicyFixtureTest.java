package com.shoestore.shared.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.policy.fixture.StandardTestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestCancellationContext;
import com.shoestore.shared.domain.policy.fixture.TestCancellationDecision;
import com.shoestore.shared.domain.policy.fixture.TestCancellationOrderStatus;
import com.shoestore.shared.domain.policy.fixture.TestFulfillmentStatus;
import com.shoestore.shared.domain.policy.fixture.TestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestPaymentStatus;
import org.junit.jupiter.api.Test;

class DomainPolicyFixtureTest {

  private final TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

  @Test
  void shouldExposeBusinessSpecificPolicyContract() {
    assertThat(policy).isInstanceOf(TestOrderCancellationPolicy.class);
  }

  @Test
  void shouldAllowDraftOrderCancellation() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.DRAFT,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.NOT_STARTED);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.ALLOWED);
  }

  @Test
  void shouldAllowConfirmedUnsettledAndUnshippedOrderCancellation() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.AUTHORIZED,
            TestFulfillmentStatus.PROCESSING);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.ALLOWED);
  }

  @Test
  void shouldRejectAlreadyCancelledOrder() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CANCELLED,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.NOT_STARTED);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_CANCELLED);
  }

  @Test
  void shouldRejectAlreadyShippedOrder() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.PENDING,
            TestFulfillmentStatus.SHIPPED);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_ALREADY_SHIPPED);
  }

  @Test
  void shouldRejectOrderWithSettledPayment() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.PROCESSING);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(decision).isEqualTo(TestCancellationDecision.REJECTED_PAYMENT_SETTLED);
  }

  @Test
  void shouldApplyAlreadyCancelledRuleBeforeOtherRejections() {
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
  void shouldRejectNullEvaluationContext() {
    assertThatNullPointerException()
        .isThrownBy(() -> policy.evaluate(null))
        .withMessage("context must not be null");
  }
}
