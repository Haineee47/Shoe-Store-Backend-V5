package com.shoestore.shared.domain.policy.fixture;

/** Typed business decisions produced by the cancellation-policy fixture. */
public enum TestCancellationDecision {
  ALLOWED,

  REJECTED_ALREADY_CANCELLED,

  REJECTED_ALREADY_SHIPPED,

  REJECTED_PAYMENT_SETTLED
}
