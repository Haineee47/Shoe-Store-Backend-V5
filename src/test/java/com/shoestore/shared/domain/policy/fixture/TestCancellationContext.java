package com.shoestore.shared.domain.policy.fixture;

import java.util.Objects;

/**
 * Immutable input used when evaluating the test order-cancellation policy.
 *
 * @param orderStatus current order status
 * @param paymentStatus current payment status
 * @param fulfillmentStatus current fulfillment status
 */
public record TestCancellationContext(
    TestCancellationOrderStatus orderStatus,
    TestPaymentStatus paymentStatus,
    TestFulfillmentStatus fulfillmentStatus) {

  /** Validates the required domain input. */
  public TestCancellationContext {
    Objects.requireNonNull(orderStatus, "orderStatus must not be null");

    Objects.requireNonNull(paymentStatus, "paymentStatus must not be null");

    Objects.requireNonNull(fulfillmentStatus, "fulfillmentStatus must not be null");
  }
}
