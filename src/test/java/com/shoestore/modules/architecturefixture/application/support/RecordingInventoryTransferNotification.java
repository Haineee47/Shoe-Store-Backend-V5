package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.application.port.InventoryTransferNotificationPort;
import com.shoestore.modules.architecturefixture.domain.event.TestInventoryTransferred;

public final class RecordingInventoryTransferNotification
    implements InventoryTransferNotificationPort {

  private int callCount;
  private TestInventoryTransferred lastEvent;

  @Override
  public void notifyTransferCompleted(TestInventoryTransferred event) {

    lastEvent = event;
    callCount++;
  }

  public int callCount() {
    return callCount;
  }

  public TestInventoryTransferred lastEvent() {
    return lastEvent;
  }
}
