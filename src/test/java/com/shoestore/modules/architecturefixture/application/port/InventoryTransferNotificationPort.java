package com.shoestore.modules.architecturefixture.application.port;

import com.shoestore.modules.architecturefixture.domain.event.TestInventoryTransferred;

public interface InventoryTransferNotificationPort {

  void notifyTransferCompleted(TestInventoryTransferred event);
}
