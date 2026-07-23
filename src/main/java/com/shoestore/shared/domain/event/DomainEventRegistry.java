package com.shoestore.shared.domain.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maintains the pending domain events of one aggregate root.
 *
 * <p>The registry preserves event registration order and exposes immutable snapshots. It does not
 * publish, serialize, persist, or handle events.
 *
 * <p>Each aggregate root must own its own registry instance. The registry must not be shared
 * through a singleton or static field.
 */
public final class DomainEventRegistry {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /**
   * Registers a domain event produced by successful aggregate behavior.
   *
   * @param domainEvent event to register
   * @throws NullPointerException when the event is {@code null}
   */
  public void register(DomainEvent domainEvent) {
    domainEvents.add(Objects.requireNonNull(domainEvent, "domainEvent must not be null"));
  }

  /**
   * Returns an immutable snapshot of currently pending events.
   *
   * <p>The returned collection is not a live view. Future registrations or clearing operations do
   * not change a previously captured snapshot.
   *
   * @return immutable event snapshot in registration order
   */
  public List<DomainEvent> domainEvents() {
    return List.copyOf(domainEvents);
  }

  /** Clears all currently pending domain events. */
  public void clear() {
    domainEvents.clear();
  }
}
