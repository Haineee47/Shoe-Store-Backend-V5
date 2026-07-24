package com.shoestore.modules.architecturefixture.application.port;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;

public interface OrderCompletionNotificationPort {

  void notifyOrderCompleted(TestOrderId orderId);
}
