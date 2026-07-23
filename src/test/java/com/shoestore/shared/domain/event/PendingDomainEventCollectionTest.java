package com.shoestore.shared.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.shared.domain.event.fixture.TestCustomerId;
import com.shoestore.shared.domain.event.fixture.TestMoney;
import com.shoestore.shared.domain.event.fixture.TestOrderAggregate;
import com.shoestore.shared.domain.event.fixture.TestOrderConfirmedDomainEvent;
import com.shoestore.shared.domain.event.fixture.TestOrderId;
import com.shoestore.shared.domain.event.fixture.TestOrderItemAddedDomainEvent;
import com.shoestore.shared.domain.event.fixture.TestOrderItemId;
import com.shoestore.shared.domain.event.fixture.TestOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PendingDomainEventCollectionTest {
  private static final TestOrderId ORDER_ID =
      new TestOrderId(UUID.fromString("4173a02a-6d34-43a9-a4b9-9912c1bc6eb4"));
  private static final TestCustomerId CUSTOMER_ID =
      new TestCustomerId(UUID.fromString("a040d329-62b7-4ea3-a789-471bf91fb305"));
  private static final TestOrderItemId INITIAL_ITEM_ID =
      new TestOrderItemId(UUID.fromString("c97d40b7-5f77-43c2-a397-b8d88d40e3cb"));
  private static final TestOrderItemId ADDED_ITEM_ID =
      new TestOrderItemId(UUID.fromString("8ee07b9b-ddde-4fef-9bb1-f9f38aa06067"));
  private static final TestMoney TOTAL =
      new TestMoney(new BigDecimal("2500000.00"), Currency.getInstance("VND"));
  private static final DomainEventMetadata ITEM_ADDED_METADATA =
      new DomainEventMetadata(
          UUID.fromString("7a3b8c06-0c54-4a38-a07d-e3c32ff388f1"),
          Instant.parse("2026-07-22T06:00:00Z"));
  private static final DomainEventMetadata ORDER_CONFIRMED_METADATA =
      new DomainEventMetadata(
          UUID.fromString("2b767252-739a-46c4-a538-ad7285e89409"),
          Instant.parse("2026-07-22T06:01:00Z"));

  @Test
  void shouldPreserveEventTypesInRegistrationOrder() {
    TestOrderAggregate order = newDraftOrder();

    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);

    order.confirm(ORDER_CONFIRMED_METADATA);

    List<DomainEvent> events = order.domainEvents();

    assertThat(events).hasSize(2);

    assertThat(events.get(0)).isExactlyInstanceOf(TestOrderItemAddedDomainEvent.class);

    assertThat(events.get(1)).isExactlyInstanceOf(TestOrderConfirmedDomainEvent.class);
  }

  @Test
  void shouldPreserveExactEventOccurrencesInOrder() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    order.confirm(ORDER_CONFIRMED_METADATA);
    TestOrderItemAddedDomainEvent itemAddedEvent =
        (TestOrderItemAddedDomainEvent) order.domainEvents().get(0);
    TestOrderConfirmedDomainEvent confirmedEvent =
        (TestOrderConfirmedDomainEvent) order.domainEvents().get(1);
    assertThat(itemAddedEvent.eventId()).isEqualTo(ITEM_ADDED_METADATA.eventId());
    assertThat(itemAddedEvent.occurredAt()).isEqualTo(ITEM_ADDED_METADATA.occurredAt());
    assertThat(confirmedEvent.eventId()).isEqualTo(ORDER_CONFIRMED_METADATA.eventId());
    assertThat(confirmedEvent.occurredAt()).isEqualTo(ORDER_CONFIRMED_METADATA.occurredAt());
  }

  @Test
  void shouldNotSortEventsByType() {
    TestOrderAggregate order = newDraftOrder();

    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);

    order.confirm(ORDER_CONFIRMED_METADATA);

    List<DomainEvent> events = order.domainEvents();

    assertThat(events).hasSize(2);

    assertThat(events.get(0)).isInstanceOf(TestOrderItemAddedDomainEvent.class);

    assertThat(events.get(1)).isInstanceOf(TestOrderConfirmedDomainEvent.class);
  }

  @Test
  void shouldNotSortEventsByOccurredAt() {
    TestOrderAggregate order = newDraftOrder();
    DomainEventMetadata laterItemAddedMetadata =
        new DomainEventMetadata(
            UUID.fromString("3c30dab2-c177-449b-a235-53691590e4ac"),
            Instant.parse("2026-07-22T07:00:00Z"));
    DomainEventMetadata earlierConfirmationMetadata =
        new DomainEventMetadata(
            UUID.fromString("e514c3bd-ac06-4727-bfe2-39d856b11b8e"),
            Instant.parse("2026-07-22T06:00:00Z"));
    order.addItem(laterItemAddedMetadata, ADDED_ITEM_ID);
    order.confirm(earlierConfirmationMetadata);
    assertThat(order.domainEvents().stream().map(DomainEvent::eventId).toList())
        .containsExactly(laterItemAddedMetadata.eventId(), earlierConfirmationMetadata.eventId());
  }

  @Test
  void shouldCaptureConfirmationPayloadAfterPriorBehavior() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    order.confirm(ORDER_CONFIRMED_METADATA);
    TestOrderConfirmedDomainEvent confirmedEvent =
        (TestOrderConfirmedDomainEvent) order.domainEvents().get(1);
    assertThat(confirmedEvent.confirmedItemIds()).containsExactly(INITIAL_ITEM_ID, ADDED_ITEM_ID);
  }

  @Test
  void shouldExposeImmutablePendingEventSnapshot() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    List<DomainEvent> snapshot = order.domainEvents();
    assertThatThrownBy(() -> snapshot.clear()).isInstanceOf(UnsupportedOperationException.class);
    assertThat(order.domainEvents()).hasSize(1);
  }

  @Test
  void shouldKeepPreviousSnapshotStableWhenNewEventIsRegistered() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    List<DomainEvent> firstSnapshot = order.domainEvents();
    order.confirm(ORDER_CONFIRMED_METADATA);
    assertThat(firstSnapshot).hasSize(1);
    assertThat(firstSnapshot.getFirst()).isInstanceOf(TestOrderItemAddedDomainEvent.class);
    assertThat(order.domainEvents()).hasSize(2);
  }

  @Test
  void shouldKeepCapturedSnapshotStableAfterClear() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    order.confirm(ORDER_CONFIRMED_METADATA);
    List<DomainEvent> capturedEvents = order.domainEvents();
    order.clearDomainEvents();
    assertThat(capturedEvents).hasSize(2);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldClearAllPendingEventsAsOneBatch() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    order.confirm(ORDER_CONFIRMED_METADATA);
    order.clearDomainEvents();
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotChangeBusinessStateWhenPendingEventsAreCleared() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    order.confirm(ORDER_CONFIRMED_METADATA);
    order.clearDomainEvents();
    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);
    assertThat(order.itemIds()).containsExactly(INITIAL_ITEM_ID, ADDED_ITEM_ID);
  }

  @Test
  void shouldMaintainIndependentCollectionsPerAggregateInstance() {
    TestOrderAggregate firstOrder = newDraftOrder();
    TestOrderAggregate secondOrder = newDraftOrder();
    firstOrder.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    assertThat(firstOrder.domainEvents()).hasSize(1);
    assertThat(secondOrder.domainEvents()).isEmpty();
  }

  @Test
  void shouldClearOnlyTheSelectedAggregateInstance() {
    TestOrderAggregate firstOrder = newDraftOrder();
    TestOrderAggregate secondOrder = newDraftOrder();
    firstOrder.addItem(ITEM_ADDED_METADATA, ADDED_ITEM_ID);
    secondOrder.addItem(
        new DomainEventMetadata(
            UUID.fromString("b8fdba29-4c4a-4712-be19-bac6701d3976"),
            Instant.parse("2026-07-22T06:02:00Z")),
        new TestOrderItemId(UUID.fromString("617230ba-6de6-4eb8-a7f7-e15e48eef42f")));
    firstOrder.clearDomainEvents();
    assertThat(firstOrder.domainEvents()).isEmpty();
    assertThat(secondOrder.domainEvents()).hasSize(1);
  }

  private static TestOrderAggregate newDraftOrder() {
    return new TestOrderAggregate(
        ORDER_ID, CUSTOMER_ID, List.of(INITIAL_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
  }
}
