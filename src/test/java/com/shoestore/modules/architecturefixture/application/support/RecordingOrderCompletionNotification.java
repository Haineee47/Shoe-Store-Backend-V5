package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.application.port.OrderCompletionNotificationPort;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.List;
import java.util.Objects;

public final class RecordingOrderCompletionNotification implements OrderCompletionNotificationPort {

  private final List<String> operations;
  private TestOrderId notifiedOrderId;
  private int callCount;

  public RecordingOrderCompletionNotification(List<String> operations) {

    this.operations = Objects.requireNonNull(operations, "operations must not be null");
  }

  @Override
  public void notifyOrderCompleted(TestOrderId orderId) {

    notifiedOrderId = Objects.requireNonNull(orderId, "orderId must not be null");

    if (!operations.contains("save")) {
      throw new IllegalStateException("notification must occur after save");
    }

    operations.add("notify");
    callCount++;
  }

  public TestOrderId notifiedOrderId() {
    return notifiedOrderId;
  }

  public int callCount() {
    return callCount;
  }
}
