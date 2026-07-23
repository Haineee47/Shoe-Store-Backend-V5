package com.shoestore.shared.domain.policy.fixture;

import java.util.Objects;

/** Stateless and deterministic test implementation of an order-cancellation policy. */
public final class StandardTestOrderCancellationPolicy implements TestOrderCancellationPolicy {

  /**
   * Evaluates cancellation rules using their defined business precedence.
   *
   * <p>The evaluation order is:
   *
   * <ol>
   *   <li>already cancelled;
   *   <li>already shipped;
   *   <li>payment settled;
   *   <li>otherwise allowed.
   * </ol>
   *
   * @param context immutable cancellation context
   * @return typed cancellation decision
   */
  @Override
  public TestCancellationDecision evaluate(TestCancellationContext context) {

    Objects.requireNonNull(context, "context must not be null");

    if (context.orderStatus() == TestCancellationOrderStatus.CANCELLED) {

      return TestCancellationDecision.REJECTED_ALREADY_CANCELLED;
    }

    if (context.fulfillmentStatus() == TestFulfillmentStatus.SHIPPED) {

      return TestCancellationDecision.REJECTED_ALREADY_SHIPPED;
    }

    if (context.paymentStatus() == TestPaymentStatus.SETTLED) {

      return TestCancellationDecision.REJECTED_PAYMENT_SETTLED;
    }

    return TestCancellationDecision.ALLOWED;
  }
}
