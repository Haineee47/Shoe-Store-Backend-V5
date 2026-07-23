package com.shoestore.shared.domain.event.fixture;

import com.shoestore.shared.domain.event.DomainEvent;
import com.shoestore.shared.domain.event.DomainEventMetadata;
import com.shoestore.shared.domain.event.DomainEventRegistry;
import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestOrderErrorCode;
import com.shoestore.shared.domain.model.AggregateRoot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * * Test-only aggregate used to verify domain-event conventions. * *
 *
 * <p>* Construction and reconstitution restore aggregate state only. * They never register domain
 * events. *
 */
public final class TestOrderAggregate implements AggregateRoot {
  private final DomainEventRegistry domainEventRegistry = new DomainEventRegistry();
  private final TestOrderId orderId;
  private final TestCustomerId customerId;
  private final List<TestOrderItemId> itemIds;
  private final TestMoney total;
  private TestOrderStatus status;

  /**
   * * Creates an aggregate fixture with the supplied state. * *
   *
   * <p>* Construction does not represent a successful business behavior and * therefore does not
   * register domain events. *
   */
  public TestOrderAggregate(
      TestOrderId orderId,
      TestCustomerId customerId,
      List<TestOrderItemId> itemIds,
      TestMoney total,
      TestOrderStatus status) {
    this.orderId = requireOrderId(orderId);
    this.customerId = requireCustomerId(customerId);
    this.itemIds = copyItemIds(itemIds);
    this.total = requireTotal(total);
    this.status = requireStatus(status);
  }

  /**
   * * Reconstitutes an aggregate from previously persisted state. * *
   *
   * <p>* Reconstitution restores state only. It must not recreate historical * domain events or
   * require new event metadata. * * * @param orderId persisted aggregate identity * @param
   * customerId persisted customer identity * @param itemIds persisted order items * @param total
   * persisted order total * @param status persisted order status * @return reconstituted aggregate
   * with no pending domain events
   */
  public static TestOrderAggregate reconstitute(
      TestOrderId orderId,
      TestCustomerId customerId,
      List<TestOrderItemId> itemIds,
      TestMoney total,
      TestOrderStatus status) {
    return new TestOrderAggregate(orderId, customerId, itemIds, total, status);
  }

  /** * Adds an item through a successful business behavior. */
  public void addItem(DomainEventMetadata eventMetadata, TestOrderItemId itemId) {
    Objects.requireNonNull(eventMetadata, "eventMetadata must not be null");
    Objects.requireNonNull(itemId, "itemId must not be null");
    ensureCanBeModified();
    itemIds.add(itemId);
    registerDomainEvent(
        new TestOrderItemAddedDomainEvent(
            eventMetadata.eventId(), eventMetadata.occurredAt(), orderId, itemId));
  }

  /** * Confirms the order through a successful business behavior. */
  public void confirm(DomainEventMetadata eventMetadata) {
    Objects.requireNonNull(eventMetadata, "eventMetadata must not be null");
    ensureCanBeConfirmed();
    List<TestOrderItemId> confirmedItemIds = List.copyOf(itemIds);
    status = TestOrderStatus.CONFIRMED;
    registerDomainEvent(
        new TestOrderConfirmedDomainEvent(
            eventMetadata.eventId(),
            eventMetadata.occurredAt(),
            orderId,
            customerId,
            confirmedItemIds,
            total));
  }

  private void ensureCanBeModified() {
    if (status != TestOrderStatus.DRAFT) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_CANNOT_BE_MODIFIED, "Only draft orders can be modified");
    }
  }

  private void ensureCanBeConfirmed() {
    if (status != TestOrderStatus.DRAFT) {
      throw new TestDomainException(
          TestOrderErrorCode.ORDER_CANNOT_BE_CONFIRMED, "Only draft orders can be confirmed");
    }
  }

  private void registerDomainEvent(DomainEvent domainEvent) {
    domainEventRegistry.register(domainEvent);
  }

  @Override
  public List<DomainEvent> domainEvents() {
    return domainEventRegistry.domainEvents();
  }

  @Override
  public void clearDomainEvents() {
    domainEventRegistry.clear();
  }

  public TestOrderId orderId() {
    return orderId;
  }

  public TestCustomerId customerId() {
    return customerId;
  }

  public List<TestOrderItemId> itemIds() {
    return List.copyOf(itemIds);
  }

  public TestMoney total() {
    return total;
  }

  public TestOrderStatus status() {
    return status;
  }

  private static TestOrderId requireOrderId(TestOrderId orderId) {
    return Objects.requireNonNull(orderId, "orderId must not be null");
  }

  private static TestCustomerId requireCustomerId(TestCustomerId customerId) {
    return Objects.requireNonNull(customerId, "customerId must not be null");
  }

  private static List<TestOrderItemId> copyItemIds(List<TestOrderItemId> itemIds) {
    Objects.requireNonNull(itemIds, "itemIds must not be null");
    if (itemIds.isEmpty()) {
      throw new IllegalArgumentException("itemIds must not be empty");
    }
    if (itemIds.stream().anyMatch(Objects::isNull)) {
      throw new NullPointerException("itemIds must not contain null elements");
    }
    return new ArrayList<>(itemIds);
  }

  private static TestMoney requireTotal(TestMoney total) {
    return Objects.requireNonNull(total, "total must not be null");
  }

  private static TestOrderStatus requireStatus(TestOrderStatus status) {
    return Objects.requireNonNull(status, "status must not be null");
  }
}
