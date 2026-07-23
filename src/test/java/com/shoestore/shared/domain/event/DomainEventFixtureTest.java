package com.shoestore.shared.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.shared.domain.event.fixture.TestCustomerId;
import com.shoestore.shared.domain.event.fixture.TestMoney;
import com.shoestore.shared.domain.event.fixture.TestOrderConfirmedDomainEvent;
import com.shoestore.shared.domain.event.fixture.TestOrderId;
import com.shoestore.shared.domain.event.fixture.TestOrderItemId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventFixtureTest {

  private static final UUID EVENT_ID = UUID.fromString("3da7e0ef-fc7d-4adc-88ac-3205037389c8");

  private static final Instant OCCURRED_AT = Instant.parse("2026-07-22T03:15:30Z");

  private static final TestOrderId ORDER_ID =
      new TestOrderId(UUID.fromString("76406328-d158-4381-8f04-48e44f74aa22"));

  private static final TestCustomerId CUSTOMER_ID =
      new TestCustomerId(UUID.fromString("9ad6da58-0d0a-4a06-b949-99a2107836c0"));

  private static final TestOrderItemId FIRST_ITEM_ID =
      new TestOrderItemId(UUID.fromString("d56297e9-591d-448e-b1bc-dbd93b3877ea"));

  private static final TestOrderItemId SECOND_ITEM_ID =
      new TestOrderItemId(UUID.fromString("57db6f81-6aca-4f67-909c-e2fe700cae60"));

  private static final TestMoney CONFIRMED_TOTAL =
      new TestMoney(new BigDecimal("2500000.00"), Currency.getInstance("VND"));

  @Test
  void shouldImplementDomainEventContract() {
    TestOrderConfirmedDomainEvent event = createEvent(List.of(FIRST_ITEM_ID, SECOND_ITEM_ID));

    assertThat(event).isInstanceOf(DomainEvent.class);
  }

  @Test
  void shouldPreserveEventIdentity() {
    TestOrderConfirmedDomainEvent event = createEvent(List.of(FIRST_ITEM_ID));

    assertThat(event.eventId()).isEqualTo(EVENT_ID);
  }

  @Test
  void shouldPreserveOccurrenceTimestamp() {
    TestOrderConfirmedDomainEvent event = createEvent(List.of(FIRST_ITEM_ID));

    assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
  }

  @Test
  void shouldPreserveImmutableBusinessSnapshot() {
    TestOrderConfirmedDomainEvent event = createEvent(List.of(FIRST_ITEM_ID, SECOND_ITEM_ID));

    assertThat(event.orderId()).isEqualTo(ORDER_ID);

    assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);

    assertThat(event.confirmedItemIds()).containsExactly(FIRST_ITEM_ID, SECOND_ITEM_ID);

    assertThat(event.confirmedTotal()).isEqualTo(CONFIRMED_TOTAL);
  }

  @Test
  void shouldDefensivelyCopyMutableCollectionInput() {
    List<TestOrderItemId> mutableItemIds = new ArrayList<>();

    mutableItemIds.add(FIRST_ITEM_ID);

    TestOrderConfirmedDomainEvent event = createEvent(mutableItemIds);

    mutableItemIds.add(SECOND_ITEM_ID);

    assertThat(event.confirmedItemIds()).containsExactly(FIRST_ITEM_ID);
  }

  @Test
  void shouldExposeUnmodifiableCollectionSnapshot() {
    TestOrderConfirmedDomainEvent event = createEvent(List.of(FIRST_ITEM_ID));

    assertThatThrownBy(() -> event.confirmedItemIds().add(SECOND_ITEM_ID))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldUseValueSemanticsForEquivalentEvents() {
    TestOrderConfirmedDomainEvent first = createEvent(List.of(FIRST_ITEM_ID, SECOND_ITEM_ID));

    TestOrderConfirmedDomainEvent second = createEvent(List.of(FIRST_ITEM_ID, SECOND_ITEM_ID));

    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
  }

  @Test
  void shouldTreatDifferentEventIdsAsDifferentOccurrences() {
    TestOrderConfirmedDomainEvent first = createEvent(List.of(FIRST_ITEM_ID));

    TestOrderConfirmedDomainEvent second =
        new TestOrderConfirmedDomainEvent(
            UUID.fromString("1c15cfab-0fdb-440b-af19-b2b9914f0d5f"),
            OCCURRED_AT,
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID),
            CONFIRMED_TOTAL);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void shouldRejectNullEventId() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    null,
                    OCCURRED_AT,
                    ORDER_ID,
                    CUSTOMER_ID,
                    List.of(FIRST_ITEM_ID),
                    CONFIRMED_TOTAL))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("eventId must not be null");
  }

  @Test
  void shouldRejectNullOccurrenceTimestamp() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID, null, ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), CONFIRMED_TOTAL))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("occurredAt must not be null");
  }

  @Test
  void shouldRejectNullOrderId() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID,
                    OCCURRED_AT,
                    null,
                    CUSTOMER_ID,
                    List.of(FIRST_ITEM_ID),
                    CONFIRMED_TOTAL))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("orderId must not be null");
  }

  @Test
  void shouldRejectNullCustomerId() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID, OCCURRED_AT, ORDER_ID, null, List.of(FIRST_ITEM_ID), CONFIRMED_TOTAL))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("customerId must not be null");
  }

  @Test
  void shouldRejectNullItemCollection() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID, OCCURRED_AT, ORDER_ID, CUSTOMER_ID, null, CONFIRMED_TOTAL))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("confirmedItemIds must not be null");
  }

  @Test
  void shouldRejectEmptyItemCollection() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID, OCCURRED_AT, ORDER_ID, CUSTOMER_ID, List.of(), CONFIRMED_TOTAL))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("confirmedItemIds must not be empty");
  }

  @Test
  void shouldRejectNullItemInsideCollection() {
    List<TestOrderItemId> itemIds = new ArrayList<>();

    itemIds.add(FIRST_ITEM_ID);
    itemIds.add(null);

    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID, OCCURRED_AT, ORDER_ID, CUSTOMER_ID, itemIds, CONFIRMED_TOTAL))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldRejectNullConfirmedTotal() {
    assertThatThrownBy(
            () ->
                new TestOrderConfirmedDomainEvent(
                    EVENT_ID, OCCURRED_AT, ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("confirmedTotal must not be null");
  }

  private static TestOrderConfirmedDomainEvent createEvent(List<TestOrderItemId> itemIds) {
    return new TestOrderConfirmedDomainEvent(
        EVENT_ID, OCCURRED_AT, ORDER_ID, CUSTOMER_ID, itemIds, CONFIRMED_TOTAL);
  }
}
