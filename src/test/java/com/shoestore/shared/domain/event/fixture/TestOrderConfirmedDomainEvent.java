package com.shoestore.shared.domain.event.fixture;

import com.shoestore.shared.domain.event.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record TestOrderConfirmedDomainEvent(
    UUID eventId,
    Instant occurredAt,
    TestOrderId orderId,
    TestCustomerId customerId,
    List<TestOrderItemId> confirmedItemIds,
    TestMoney confirmedTotal)
    implements DomainEvent {

  public TestOrderConfirmedDomainEvent {
    Objects.requireNonNull(eventId, "eventId must not be null");
    Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    Objects.requireNonNull(orderId, "orderId must not be null");
    Objects.requireNonNull(customerId, "customerId must not be null");
    Objects.requireNonNull(confirmedItemIds, "confirmedItemIds must not be null");
    Objects.requireNonNull(confirmedTotal, "confirmedTotal must not be null");

    if (confirmedItemIds.isEmpty()) {
      throw new IllegalArgumentException("confirmedItemIds must not be empty");
    }

    confirmedItemIds = List.copyOf(confirmedItemIds);
  }
}
