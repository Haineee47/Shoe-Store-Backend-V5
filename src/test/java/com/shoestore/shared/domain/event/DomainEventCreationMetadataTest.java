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
import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestOrderErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventCreationMetadataTest {
  private static final TestOrderId ORDER_ID =
      new TestOrderId(UUID.fromString("61d3291f-074a-4682-a262-5252c99d46e9"));
  private static final TestCustomerId CUSTOMER_ID =
      new TestCustomerId(UUID.fromString("3e3a9e23-7b31-4fac-85ae-b0f4ba9d8b50"));
  private static final TestOrderItemId INITIAL_ITEM_ID =
      new TestOrderItemId(UUID.fromString("81145b67-2827-4731-aee8-745727882cd4"));
  private static final TestOrderItemId ADDED_ITEM_ID =
      new TestOrderItemId(UUID.fromString("5327260c-bf36-470e-ac34-fcab96c957ea"));
  private static final TestMoney TOTAL =
      new TestMoney(new BigDecimal("1900000.00"), Currency.getInstance("VND"));

  @Test
  void shouldPropagateExplicitMetadataToItemAddedEvent() {
    TestOrderAggregate order = newDraftOrder();
    UUID eventId = UUID.fromString("5d9f3295-01bb-40db-a63b-f3372c53ad9b");
    Instant occurredAt = Instant.parse("2026-07-22T08:00:00Z");
    order.addItem(new DomainEventMetadata(eventId, occurredAt), ADDED_ITEM_ID);
    TestOrderItemAddedDomainEvent event =
        (TestOrderItemAddedDomainEvent) order.domainEvents().getFirst();
    assertThat(event.eventId()).isEqualTo(eventId);
    assertThat(event.occurredAt()).isEqualTo(occurredAt);
  }

  @Test
  void shouldPropagateExplicitMetadataToConfirmedEvent() {
    TestOrderAggregate order = newDraftOrder();
    UUID eventId = UUID.fromString("25e24795-f8d6-4688-84a9-c9cafcd19a45");
    Instant occurredAt = Instant.parse("2026-07-22T08:05:00Z");
    order.confirm(new DomainEventMetadata(eventId, occurredAt));
    TestOrderConfirmedDomainEvent event =
        (TestOrderConfirmedDomainEvent) order.domainEvents().getFirst();
    assertThat(event.eventId()).isEqualTo(eventId);
    assertThat(event.occurredAt()).isEqualTo(occurredAt);
  }

  @Test
  void shouldPreserveDistinctMetadataAcrossOccurrences() {
    TestOrderAggregate order = newDraftOrder();
    DomainEventMetadata itemAddedMetadata =
        new DomainEventMetadata(
            UUID.fromString("2b132c73-8ad1-4de2-be48-6a128e376f3b"),
            Instant.parse("2026-07-22T08:10:00Z"));
    DomainEventMetadata confirmationMetadata =
        new DomainEventMetadata(
            UUID.fromString("79062f73-f051-4609-8448-b6a646c0f35b"),
            Instant.parse("2026-07-22T08:11:00Z"));
    order.addItem(itemAddedMetadata, ADDED_ITEM_ID);
    order.confirm(confirmationMetadata);
    DomainEvent firstEvent = order.domainEvents().get(0);
    DomainEvent secondEvent = order.domainEvents().get(1);
    assertThat(firstEvent.eventId()).isEqualTo(itemAddedMetadata.eventId());
    assertThat(firstEvent.occurredAt()).isEqualTo(itemAddedMetadata.occurredAt());
    assertThat(secondEvent.eventId()).isEqualTo(confirmationMetadata.eventId());
    assertThat(secondEvent.occurredAt()).isEqualTo(confirmationMetadata.occurredAt());
    assertThat(firstEvent.eventId()).isNotEqualTo(secondEvent.eventId());
  }

  @Test
  void shouldRejectNullMetadataBeforeAddingItem() {
    TestOrderAggregate order = newDraftOrder();
    assertThatThrownBy(() -> order.addItem(null, ADDED_ITEM_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("eventMetadata must not be null");
    assertThat(order.itemIds()).containsExactly(INITIAL_ITEM_ID);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldRejectNullMetadataBeforeConfirmation() {
    TestOrderAggregate order = newDraftOrder();
    assertThatThrownBy(() -> order.confirm(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("eventMetadata must not be null");
    assertThat(order.status()).isEqualTo(TestOrderStatus.DRAFT);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRegisterEventWhenBusinessRuleRejectsOperation() {
    TestOrderAggregate order =
        new TestOrderAggregate(
            ORDER_ID, CUSTOMER_ID, List.of(INITIAL_ITEM_ID), TOTAL, TestOrderStatus.CONFIRMED);
    DomainEventMetadata metadata =
        new DomainEventMetadata(
            UUID.fromString("1c4cd8dd-f7dc-47d5-a986-7b33508148ef"),
            Instant.parse("2026-07-22T08:20:00Z"));
    assertThatThrownBy(() -> order.addItem(metadata, ADDED_ITEM_ID))
        .isInstanceOf(TestDomainException.class)
        .satisfies(
            exception -> {
              TestDomainException domainException = (TestDomainException) exception;
              assertThat(domainException.errorCode())
                  .isEqualTo(TestOrderErrorCode.ORDER_CANNOT_BE_MODIFIED);
            });
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldCapturePayloadAfterSuccessfulPriorBehavior() {
    TestOrderAggregate order = newDraftOrder();
    order.addItem(
        new DomainEventMetadata(
            UUID.fromString("da433d76-7bd0-44c9-a835-f8ca6f9c802d"),
            Instant.parse("2026-07-22T08:30:00Z")),
        ADDED_ITEM_ID);
    order.confirm(
        new DomainEventMetadata(
            UUID.fromString("39614737-d237-4a59-a181-7450409a33a8"),
            Instant.parse("2026-07-22T08:31:00Z")));
    TestOrderConfirmedDomainEvent event =
        (TestOrderConfirmedDomainEvent) order.domainEvents().get(1);
    assertThat(event.confirmedItemIds()).containsExactly(INITIAL_ITEM_ID, ADDED_ITEM_ID);
  }

  @Test
  void shouldPreserveSuppliedOccurredAtWithoutModification() {
    TestOrderAggregate order = newDraftOrder();
    Instant suppliedOccurredAt = Instant.parse("2000-01-01T00:00:00Z");
    order.confirm(
        new DomainEventMetadata(
            UUID.fromString("43e5691a-dba8-4e42-88a4-bf416e4fc27d"), suppliedOccurredAt));
    DomainEvent event = order.domainEvents().getFirst();
    assertThat(event.occurredAt()).isEqualTo(suppliedOccurredAt);
  }

  private static TestOrderAggregate newDraftOrder() {
    return new TestOrderAggregate(
        ORDER_ID, CUSTOMER_ID, List.of(INITIAL_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
  }
}
