package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.domain.model.TestInventoryAggregate;
import com.shoestore.modules.architecturefixture.domain.repository.TestInventoryRepository;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TrackingTestInventoryRepository implements TestInventoryRepository {

  private final Map<TestInventoryId, TestInventoryAggregate> storage = new LinkedHashMap<>();

  private Map<TestInventoryId, TestInventoryAggregate> snapshot = Map.of();

  private int saveCallCount;
  private int failOnSaveCall = -1;

  public void seed(TestInventoryAggregate inventory) {

    Objects.requireNonNull(inventory, "inventory must not be null");

    storage.put(inventory.id(), copyOf(inventory));
  }

  public void beginTransaction() {

    Map<TestInventoryId, TestInventoryAggregate> copy = new LinkedHashMap<>();

    storage.forEach((id, inventory) -> copy.put(id, copyOf(inventory)));

    snapshot = copy;
    saveCallCount = 0;
  }

  public void commitTransaction() {
    snapshot = Map.of();
  }

  public void rollbackTransaction() {

    storage.clear();

    snapshot.forEach((id, inventory) -> storage.put(id, copyOf(inventory)));

    snapshot = Map.of();
  }

  public void failOnSaveCall(int callNumber) {

    failOnSaveCall = callNumber;
  }

  @Override
  public Optional<TestInventoryAggregate> findById(TestInventoryId inventoryId) {

    return Optional.ofNullable(storage.get(inventoryId));
  }

  @Override
  public TestInventoryAggregate save(TestInventoryAggregate inventory) {

    saveCallCount++;

    if (saveCallCount == failOnSaveCall) {
      throw new IllegalStateException("simulated repository failure");
    }

    storage.put(inventory.id(), inventory);

    return inventory;
  }

  private static TestInventoryAggregate copyOf(TestInventoryAggregate inventory) {

    return new TestInventoryAggregate(inventory.id(), inventory.availableQuantity());
  }
}
