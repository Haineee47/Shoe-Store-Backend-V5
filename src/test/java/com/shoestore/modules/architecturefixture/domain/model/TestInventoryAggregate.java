package com.shoestore.modules.architecturefixture.domain.model;

import com.shoestore.modules.architecturefixture.domain.event.TestInventoryTransferred;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TestInventoryAggregate {

  private final TestInventoryId id;
  private int availableQuantity;
  private final List<Object> domainEvents = new ArrayList<>();

  public TestInventoryAggregate(TestInventoryId id, int availableQuantity) {

    this.id = Objects.requireNonNull(id, "id must not be null");

    if (availableQuantity < 0) {
      throw new IllegalArgumentException("availableQuantity must not be negative");
    }

    this.availableQuantity = availableQuantity;
  }

  public TestInventoryId id() {
    return id;
  }

  public int availableQuantity() {
    return availableQuantity;
  }

  public void remove(int quantity, TestInventoryId destinationInventoryId) {

    Objects.requireNonNull(destinationInventoryId, "destinationInventoryId must not be null");

    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be greater than zero");
    }

    if (availableQuantity < quantity) {
      throw new IllegalStateException("insufficient inventory");
    }

    availableQuantity -= quantity;

    domainEvents.add(new TestInventoryTransferred(id, destinationInventoryId, quantity));
  }

  public void add(int quantity) {

    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be greater than zero");
    }

    availableQuantity += quantity;
  }

  public List<Object> pullDomainEvents() {

    List<Object> events = List.copyOf(domainEvents);

    domainEvents.clear();

    return events;
  }
}
