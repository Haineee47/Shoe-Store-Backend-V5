package com.shoestore.modules.architecturefixture.domain.event;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.shared.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Test-only Domain Event used to verify repository retrieval semantics. */
public record TestRepositoryDomainEvent(
    UUID eventId, TestRepositoryAggregateId aggregateId, Instant occurredAt)
    implements DomainEvent {

  public TestRepositoryDomainEvent {
    Objects.requireNonNull(eventId, "eventId must not be null");

    Objects.requireNonNull(aggregateId, "aggregateId must not be null");

    Objects.requireNonNull(occurredAt, "occurredAt must not be null");
  }
}
