package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.domain.model.TestOrderAggregate;
import com.shoestore.modules.architecturefixture.domain.repository.TestOrderRepository;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TrackingTestOrderRepository implements TestOrderRepository {

  private final Map<TestOrderId, TestOrderAggregate> storage = new LinkedHashMap<>();

  private final List<String> operations;

  public TrackingTestOrderRepository(List<String> operations) {

    this.operations = Objects.requireNonNull(operations, "operations must not be null");
  }

  public void seed(TestOrderAggregate order) {

    Objects.requireNonNull(order, "order must not be null");

    storage.put(order.id(), order);
  }

  @Override
  public Optional<TestOrderAggregate> findById(TestOrderId orderId) {

    Objects.requireNonNull(orderId, "orderId must not be null");

    operations.add("load");

    return Optional.ofNullable(storage.get(orderId));
  }

  @Override
  public TestOrderAggregate save(TestOrderAggregate order) {

    Objects.requireNonNull(order, "order must not be null");

    if (!order.isCompleted()) {
      throw new IllegalStateException("order must be completed before save");
    }

    if (!order.areAllLinesFulfilled()) {
      throw new IllegalStateException("all order lines must be fulfilled before save");
    }

    operations.add("save");
    storage.put(order.id(), order);

    return order;
  }
}
