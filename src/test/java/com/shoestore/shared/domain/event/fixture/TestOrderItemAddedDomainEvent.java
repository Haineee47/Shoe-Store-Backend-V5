package com.shoestore.shared.domain.event.fixture;

import com.shoestore.shared.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Test-only domain event representing an item successfully added to an order.
 *
 * @param eventId unique identity of this event occurrence
 * @param occurredAt time at which the item was added
 * @param orderId aggregate identity
 * @param itemId identity of the added item
 */
public record TestOrderItemAddedDomainEvent(
    UUID eventId, Instant occurredAt, TestOrderId orderId, TestOrderItemId itemId)
    implements DomainEvent {

  public TestOrderItemAddedDomainEvent {
    Objects.requireNonNull(eventId, "eventId must not be null");
    Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    Objects.requireNonNull(orderId, "orderId must not be null");
    Objects.requireNonNull(itemId, "itemId must not be null");
  }
}
