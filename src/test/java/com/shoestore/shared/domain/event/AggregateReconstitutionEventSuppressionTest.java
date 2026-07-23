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
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AggregateReconstitutionEventSuppressionTest {
  private static final TestOrderId ORDER_ID =
      new TestOrderId(UUID.fromString("c0efc70c-095e-4c70-a1b6-01e710f78840"));
  private static final TestCustomerId CUSTOMER_ID =
      new TestCustomerId(UUID.fromString("b649ae15-494f-4dcf-8564-9f31e592d986"));
  private static final TestOrderItemId FIRST_ITEM_ID =
      new TestOrderItemId(UUID.fromString("82aeb0ab-6009-442f-90ab-2fef868fdbe6"));
  private static final TestOrderItemId SECOND_ITEM_ID =
      new TestOrderItemId(UUID.fromString("7aa2a54f-b55c-4d27-b588-dc17f8db5a59"));
  private static final TestOrderItemId NEW_ITEM_ID =
      new TestOrderItemId(UUID.fromString("db2cc62b-c08a-440d-aab6-8392f7969d19"));
  private static final TestMoney TOTAL =
      new TestMoney(new BigDecimal("3200000.00"), Currency.getInstance("VND"));

  @Test
  void shouldNotRegisterEventsWhenConstructingAggregate() {
    TestOrderAggregate order =
        new TestOrderAggregate(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.CONFIRMED);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRegisterEventsWhenReconstitutingDraftAggregate() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRecreateConfirmationEventWhenReconstitutingConfirmedAggregate() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.CONFIRMED);
    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRecreateItemAddedEventsFromPersistedItems() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.DRAFT);
    assertThat(order.itemIds()).containsExactly(FIRST_ITEM_ID, SECOND_ITEM_ID);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldRestorePersistedStateWithoutEventMetadata() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.CONFIRMED);
    assertThat(order.orderId()).isEqualTo(ORDER_ID);
    assertThat(order.customerId()).isEqualTo(CUSTOMER_ID);
    assertThat(order.itemIds()).containsExactly(FIRST_ITEM_ID, SECOND_ITEM_ID);
    assertThat(order.total()).isEqualTo(TOTAL);
    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldDefensivelyCopyPersistedCollectionDuringReconstitution() {
    List<TestOrderItemId> persistedItemIds = new ArrayList<>(List.of(FIRST_ITEM_ID));
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, persistedItemIds, TOTAL, TestOrderStatus.DRAFT);
    persistedItemIds.add(SECOND_ITEM_ID);
    assertThat(order.itemIds()).containsExactly(FIRST_ITEM_ID);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldAllowNewBusinessEventAfterDraftAggregateIsReconstituted() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
    DomainEventMetadata metadata =
        new DomainEventMetadata(
            UUID.fromString("68e46c90-d0b2-49a5-b087-f887d11ba09c"),
            Instant.parse("2026-07-22T09:00:00Z"));
    order.addItem(metadata, NEW_ITEM_ID);
    assertThat(order.itemIds()).containsExactly(FIRST_ITEM_ID, NEW_ITEM_ID);
    assertThat(order.domainEvents()).hasSize(1);
    assertThat(order.domainEvents().getFirst())
        .isExactlyInstanceOf(TestOrderItemAddedDomainEvent.class);
  }

  @Test
  void shouldRegisterOnlyNewEventAfterReconstitution() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.DRAFT);
    order.confirm(
        new DomainEventMetadata(
            UUID.fromString("077dc4f0-c004-46a2-b38f-f405a7d9f59c"),
            Instant.parse("2026-07-22T09:05:00Z")));
    assertThat(order.domainEvents()).hasSize(1);
    assertThat(order.domainEvents().getFirst())
        .isExactlyInstanceOf(TestOrderConfirmedDomainEvent.class);
  }

  @Test
  void shouldNotIncludeHistoricalItemEventsAfterNewConfirmation() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.DRAFT);
    order.confirm(
        new DomainEventMetadata(
            UUID.fromString("64e6a788-e290-4d07-8a47-cbca20efacb9"),
            Instant.parse("2026-07-22T09:10:00Z")));
    assertThat(order.domainEvents())
        .singleElement()
        .isExactlyInstanceOf(TestOrderConfirmedDomainEvent.class);
  }

  @Test
  void shouldCaptureReconstitutedStateInNewConfirmationEvent() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID,
            CUSTOMER_ID,
            List.of(FIRST_ITEM_ID, SECOND_ITEM_ID),
            TOTAL,
            TestOrderStatus.DRAFT);
    order.confirm(
        new DomainEventMetadata(
            UUID.fromString("c346467e-62ae-430a-99f7-b680812dcdbd"),
            Instant.parse("2026-07-22T09:15:00Z")));
    TestOrderConfirmedDomainEvent event =
        (TestOrderConfirmedDomainEvent) order.domainEvents().getFirst();
    assertThat(event.confirmedItemIds()).containsExactly(FIRST_ITEM_ID, SECOND_ITEM_ID);
  }

  @Test
  void shouldNotRegisterEventWhenBehaviorFailsAfterReconstitution() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.CONFIRMED);
    assertThatThrownBy(
            () ->
                order.addItem(
                    new DomainEventMetadata(
                        UUID.fromString("f74430a2-2cc0-4fa2-9e09-15c031b22113"),
                        Instant.parse("2026-07-22T09:20:00Z")),
                    NEW_ITEM_ID))
        .isInstanceOf(TestDomainException.class)
        .satisfies(
            exception -> {
              TestDomainException domainException = (TestDomainException) exception;
              assertThat(domainException.errorCode())
                  .isEqualTo(TestOrderErrorCode.ORDER_CANNOT_BE_MODIFIED);
            });
    assertThat(order.status()).isEqualTo(TestOrderStatus.CONFIRMED);
    assertThat(order.itemIds()).containsExactly(FIRST_ITEM_ID);
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRegisterDuplicateConfirmationAfterReconstitution() {
    TestOrderAggregate order =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.CONFIRMED);
    assertThatThrownBy(
            () ->
                order.confirm(
                    new DomainEventMetadata(
                        UUID.fromString("b824c115-63a1-416d-bf77-a569492efad7"),
                        Instant.parse("2026-07-22T09:25:00Z"))))
        .isInstanceOf(TestDomainException.class)
        .satisfies(
            exception -> {
              TestDomainException domainException = (TestDomainException) exception;
              assertThat(domainException.errorCode())
                  .isEqualTo(TestOrderErrorCode.ORDER_CANNOT_BE_CONFIRMED);
            });
    assertThat(order.domainEvents()).isEmpty();
  }

  @Test
  void shouldCreateIndependentPendingEventRegistryForEachReconstitution() {
    TestOrderAggregate firstOrder =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
    TestOrderAggregate secondOrder =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
    firstOrder.addItem(
        new DomainEventMetadata(
            UUID.fromString("12eec295-2ce5-490b-846b-f331246b97bb"),
            Instant.parse("2026-07-22T09:30:00Z")),
        NEW_ITEM_ID);
    assertThat(firstOrder.domainEvents()).hasSize(1);
    assertThat(secondOrder.domainEvents()).isEmpty();
  }

  @Test
  void shouldNotRestorePreviouslyClearedPendingEvents() {
    TestOrderAggregate firstExecution =
        TestOrderAggregate.reconstitute(
            ORDER_ID, CUSTOMER_ID, List.of(FIRST_ITEM_ID), TOTAL, TestOrderStatus.DRAFT);
    firstExecution.confirm(
        new DomainEventMetadata(
            UUID.fromString("9252f725-b321-4aa2-943a-0aad9e6adf94"),
            Instant.parse("2026-07-22T09:35:00Z")));
    firstExecution.clearDomainEvents();
    TestOrderAggregate nextExecution =
        TestOrderAggregate.reconstitute(
            firstExecution.orderId(),
            firstExecution.customerId(),
            firstExecution.itemIds(),
            firstExecution.total(),
            firstExecution.status());
    assertThat(firstExecution.domainEvents()).isEmpty();
    assertThat(nextExecution.domainEvents()).isEmpty();
    assertThat(nextExecution.status()).isEqualTo(TestOrderStatus.CONFIRMED);
  }
}
