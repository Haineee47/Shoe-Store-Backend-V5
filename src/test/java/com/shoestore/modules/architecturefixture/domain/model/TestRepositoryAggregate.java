package com.shoestore.modules.architecturefixture.domain.model;

import com.shoestore.modules.architecturefixture.domain.event.TestRepositoryDomainEvent;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryChildId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import com.shoestore.shared.domain.event.DomainEvent;
import com.shoestore.shared.domain.model.AggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Test-only aggregate used to define Domain Repository conventions. */
public final class TestRepositoryAggregate implements AggregateRoot {

  private final TestRepositoryAggregateId id;
  private final TestRepositoryLookupKey lookupKey;
  private final List<TestRepositoryChild> children;
  private final List<DomainEvent> pendingDomainEvents;

  public TestRepositoryAggregate(TestRepositoryAggregateId id, TestRepositoryLookupKey lookupKey) {

    this.id = Objects.requireNonNull(id, "id must not be null");
    this.lookupKey = Objects.requireNonNull(lookupKey, "lookupKey must not be null");

    this.children = new ArrayList<>();
    this.pendingDomainEvents = new ArrayList<>();
  }

  public TestRepositoryAggregateId id() {
    return id;
  }

  public TestRepositoryLookupKey lookupKey() {
    return lookupKey;
  }

  public List<TestRepositoryChild> children() {
    return List.copyOf(children);
  }

  public void addChild(TestRepositoryChildId childId, String description) {

    Objects.requireNonNull(childId, "childId must not be null");

    if (findChild(childId).isPresent()) {
      throw new IllegalArgumentException("child with the same identity already exists");
    }

    children.add(new TestRepositoryChild(childId, description));
  }

  public void updateChildDescription(TestRepositoryChildId childId, String description) {

    TestRepositoryChild child =
        findChild(childId)
            .orElseThrow(() -> new IllegalArgumentException("child does not belong to aggregate"));

    child.updateDescription(description);
  }

  public Optional<TestRepositoryChild> findChild(TestRepositoryChildId childId) {

    Objects.requireNonNull(childId, "childId must not be null");

    return children.stream().filter(child -> child.id().equals(childId)).findFirst();
  }

  @Override
  public List<DomainEvent> domainEvents() {
    return List.copyOf(pendingDomainEvents);
  }

  @Override
  public void clearDomainEvents() {
    pendingDomainEvents.clear();
  }

  public void recordTestEvent() {
    pendingDomainEvents.add(new TestRepositoryDomainEvent(UUID.randomUUID(), id, Instant.now()));
  }
}
