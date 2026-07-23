package com.shoestore.shared.domain.model;

import com.shoestore.shared.domain.event.DomainEvent;
import java.util.List;

/**
 * Marks a domain entity as the root of an aggregate.
 *
 * <p>An aggregate root is the only entry point through which external components may modify the
 * state of the aggregate.
 *
 * <p>Implementations may extend either {@code BaseEntity} or {@code AuditableEntity}, depending on
 * whether persistence auditing is required.
 *
 * <p>Aggregate roots may register domain events produced by successful domain behavior. Event
 * publication remains the responsibility of an outer application boundary.
 */
public interface AggregateRoot {

  /**
   * Returns an immutable snapshot of currently pending domain events.
   *
   * @return pending events in registration order
   */
  List<DomainEvent> domainEvents();

  /** Clears all currently pending domain events. */
  void clearDomainEvents();
}
