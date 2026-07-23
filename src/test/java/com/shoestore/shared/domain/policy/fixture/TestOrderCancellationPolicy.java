package com.shoestore.shared.domain.policy.fixture;

/** Test-only business-specific policy contract for evaluating whether an order may be cancelled. */
public interface TestOrderCancellationPolicy {

  /**
   * Evaluates the supplied cancellation context.
   *
   * @param context immutable cancellation context
   * @return typed cancellation decision
   */
  TestCancellationDecision evaluate(TestCancellationContext context);
}
