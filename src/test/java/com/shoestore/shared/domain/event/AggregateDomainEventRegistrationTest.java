package com.shoestore.shared.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.shared.domain.event.fixture.TestCustomerId;
import com.shoestore.shared.domain.event.fixture.TestMoney;
import com.shoestore.shared.domain.event.fixture.TestOrderAggregate;
import com.shoestore.shared.domain.event.fixture.TestOrderConfirmedDomainEvent;
import com.shoestore.shared.domain.event.fixture.TestOrderId;
import com.shoestore.shared.domain.event.fixture.TestOrderItemId;
import com.shoestore.shared.domain.event.fixture.TestOrderStatus;
import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.model.AggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AggregateDomainEventRegistrationTest {

  private static final TestOrderId ORDER_ID =
      new TestOrderId(UUID.fromString("6a202e28-dffd-4fea-b29f-c65f74139c01"));

  private static final TestCustomerId CUSTOMER_ID =
      new TestCustomerId(UUID.fromString("2d56f86d-a5c8-47b1-9aa4-1d845d45cacf"));

  private static final TestOrderItemId ITEM_ID =
      new TestOrderItemId(UUID.fromString("79eb29f3-6d8e-470b-b55b-bdfe157fa39e"));

  private static final TestMoney TOTAL =
      new TestMoney(new BigDecimal("1500000.00"), Currency.getInstance("VND"));

  private static final UUID EVENT_ID = UUID.fromString("b4527706-8128-47cd-a057-d03ab65ade00");

  private static final Instant OCCURRED_AT = Instant.parse("2026-07-22T04:30:00Z");

  @Test
  void shouldImplementAggregateRootContract() {
    TestOrderAggregate order = newDraftOrder();

    assertThat(order).isInstanceOf(AggregateRoot.class);
  }

  @Test
  void shouldStartWithNoPendingDomainEvents() {
    TestOrderAggregate order = newDraftOrder();

    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldRegisterDomainEventAfterSuccessfulBehavior() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    assertThat(order.domainEvents()).hasSize(1);

    assertThat(order.domainEvents().getFirst()).isInstanceOf(TestOrderConfirmedDomainEvent.class);
  }

  @Test
  void shouldMutateStateAndRegisterSuccessfulFact() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);

    assertThat(order.domainEvents()).hasSize(1);
  }

  @Test
  void shouldRegisterExpectedImmutableEventSnapshot() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    TestOrderConfirmedDomainEvent event =
        (TestOrderConfirmedDomainEvent) order.domainEvents().getFirst();

    assertThat(event.eventId()).isEqualTo(EVENT_ID);

    assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);

    assertThat(event.orderId()).isEqualTo(ORDER_ID);

    assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);

    assertThat(event.confirmedItemIds()).containsExactly(ITEM_ID);

    assertThat(event.confirmedTotal()).isEqualTo(TOTAL);
  }

  @Test
  void shouldExposeUnmodifiablePendingEventSnapshot() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    List<DomainEvent> events = order.domainEvents();

    assertThatThrownBy(() -> events.add(events.getFirst()))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldReturnIndependentPendingEventSnapshot() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    List<DomainEvent> firstSnapshot = order.domainEvents();

    order.clearDomainEvents();

    assertThat(firstSnapshot).hasSize(1);

    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldClearPendingEventsWithoutChangingBusinessState() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    order.clearDomainEvents();

    assertThat(order.domainEvents()).isEmpty();

    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);
  }

  @Test
  void shouldNotRegisterEventWhenOperationFails() {
    TestOrderAggregate order =
        new TestOrderAggregate(
            ORDER_ID, CUSTOMER_ID, List.of(ITEM_ID), TOTAL, TestOrderStatus.CANCELLED);

    assertThatThrownBy(() -> order.confirm(eventMetadata()))
        .isInstanceOf(TestDomainException.class);

    assertThat(order.status()).isEqualTo(TestOrderStatus.CANCELLED);

    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRegisterDuplicateEventForRepeatedOperation() {
    TestOrderAggregate order = newDraftOrder();

    order.confirm(eventMetadata());

    DomainEvent firstEvent = order.domainEvents().getFirst();

    assertThatThrownBy(
            () ->
                order.confirm(
                    new DomainEventMetadata(
                        UUID.fromString("5ee1c18d-519b-443c-9ce6-dfb18928cd77"),
                        Instant.parse("2026-07-22T04:31:00Z"))))
        .isInstanceOf(TestDomainException.class);

    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);

    assertThat(order.domainEvents()).containsExactly(firstEvent);
  }

  @Test
  void shouldRejectNullEventMetadataBeforeMutation() {
    TestOrderAggregate order = newDraftOrder();

    assertThatThrownBy(() -> order.confirm(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("eventMetadata must not be null");

    assertThat(order.status()).isEqualTo(TestOrderStatus.DRAFT);

    assertThat(order.domainEvents()).isEmpty();
  }

  private static TestOrderAggregate newDraftOrder() {
    return new TestOrderAggregate(
        ORDER_ID, CUSTOMER_ID, List.of(ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
  }

  private static DomainEventMetadata eventMetadata() {
    return new DomainEventMetadata(EVENT_ID, OCCURRED_AT);
  }
}
