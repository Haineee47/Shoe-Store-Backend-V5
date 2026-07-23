package com.shoestore.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an immutable business fact that has occurred inside the domain.
 *
 * <p>A domain event is created by domain behavior after the corresponding business rules have been
 * satisfied and the domain state has changed successfully.
 *
 * <p>Domain events are internal domain contracts. They must remain independent of transport
 * protocols, persistence technology, logging frameworks, serialization libraries, and application
 * event mechanisms.
 */
public interface DomainEvent {

  /**
   * Returns the unique identity of this event occurrence.
   *
   * @return immutable event identifier
   */
  UUID eventId();

  /**
   * Returns the instant at which the business fact occurred.
   *
   * @return occurrence timestamp in UTC-based instant form
   */
  Instant occurredAt();
}
