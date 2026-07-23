package com.shoestore.shared.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * * Provides deterministic metadata for one domain-event occurrence. * *
 *
 * <p>* Event metadata is created by an outer application boundary and supplied * to the aggregate
 * behavior that may produce the event. * * *
 *
 * <p>* The aggregate must not generate event identity or occurrence time by * calling system APIs
 * directly. * * * @param eventId unique identity of one event occurrence * @param occurredAt time
 * at which the business fact occurred
 */
public record DomainEventMetadata(UUID eventId, Instant occurredAt) {
  public DomainEventMetadata {
    Objects.requireNonNull(eventId, "eventId must not be null");
    Objects.requireNonNull(occurredAt, "occurredAt must not be null");
  }
}
