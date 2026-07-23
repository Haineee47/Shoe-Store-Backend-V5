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
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventRegistryTest {

  @Test
  void shouldStartWithNoPendingEvents() {
    DomainEventRegistry registry = new DomainEventRegistry();

    assertThat(registry.domainEvents()).isEmpty();
  }

  @Test
  void shouldRegisterDomainEvent() {
    DomainEventRegistry registry = new DomainEventRegistry();

    DomainEvent event =
        createEvent(
            UUID.fromString("be572094-738e-4cc5-b5fb-0d26ceebbf4d"),
            Instant.parse("2026-07-22T05:00:00Z"));

    registry.register(event);

    assertThat(registry.domainEvents()).containsExactly(event);
  }

  @Test
  void shouldPreserveRegistrationOrder() {
    DomainEventRegistry registry = new DomainEventRegistry();

    DomainEvent first =
        createEvent(
            UUID.fromString("be572094-738e-4cc5-b5fb-0d26ceebbf4d"),
            Instant.parse("2026-07-22T05:00:00Z"));

    DomainEvent second =
        createEvent(
            UUID.fromString("13b794b7-a891-4be4-a02d-17b629ee383b"),
            Instant.parse("2026-07-22T05:01:00Z"));

    registry.register(first);
    registry.register(second);

    assertThat(registry.domainEvents()).containsExactly(first, second);
  }

  @Test
  void shouldAllowDistinctEventOccurrences() {
    DomainEventRegistry registry = new DomainEventRegistry();

    DomainEvent first =
        createEvent(
            UUID.fromString("be572094-738e-4cc5-b5fb-0d26ceebbf4d"),
            Instant.parse("2026-07-22T05:00:00Z"));

    DomainEvent second =
        createEvent(
            UUID.fromString("13b794b7-a891-4be4-a02d-17b629ee383b"),
            Instant.parse("2026-07-22T05:00:00Z"));

    registry.register(first);
    registry.register(second);

    assertThat(registry.domainEvents()).containsExactly(first, second);
  }

  @Test
  void shouldRejectNullDomainEvent() {
    DomainEventRegistry registry = new DomainEventRegistry();

    assertThatThrownBy(() -> registry.register(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("domainEvent must not be null");
  }

  @Test
  void shouldExposeImmutableSnapshot() {
    DomainEventRegistry registry = new DomainEventRegistry();

    DomainEvent event =
        createEvent(
            UUID.fromString("be572094-738e-4cc5-b5fb-0d26ceebbf4d"),
            Instant.parse("2026-07-22T05:00:00Z"));

    registry.register(event);

    List<DomainEvent> snapshot = registry.domainEvents();

    assertThatThrownBy(() -> snapshot.add(event)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldReturnIndependentSnapshot() {
    DomainEventRegistry registry = new DomainEventRegistry();

    DomainEvent first =
        createEvent(
            UUID.fromString("be572094-738e-4cc5-b5fb-0d26ceebbf4d"),
            Instant.parse("2026-07-22T05:00:00Z"));

    DomainEvent second =
        createEvent(
            UUID.fromString("13b794b7-a891-4be4-a02d-17b629ee383b"),
            Instant.parse("2026-07-22T05:01:00Z"));

    registry.register(first);

    List<DomainEvent> firstSnapshot = registry.domainEvents();

    registry.register(second);

    assertThat(firstSnapshot).containsExactly(first);

    assertThat(registry.domainEvents()).containsExactly(first, second);
  }

  @Test
  void shouldPreserveCapturedSnapshotAfterClear() {
    DomainEventRegistry registry = new DomainEventRegistry();

    DomainEvent event =
        createEvent(
            UUID.fromString("be572094-738e-4cc5-b5fb-0d26ceebbf4d"),
            Instant.parse("2026-07-22T05:00:00Z"));

    registry.register(event);

    List<DomainEvent> snapshot = registry.domainEvents();

    registry.clear();

    assertThat(snapshot).containsExactly(event);

    assertThat(registry.domainEvents()).isEmpty();
  }

  @Test
  void shouldAllowClearingEmptyRegistry() {
    DomainEventRegistry registry = new DomainEventRegistry();

    registry.clear();

    assertThat(registry.domainEvents()).isEmpty();
  }

  private static DomainEvent createEvent(UUID eventId, Instant occurredAt) {
    return new TestOrderConfirmedDomainEvent(
        eventId,
        occurredAt,
        new TestOrderId(UUID.fromString("211f740c-af69-466c-a895-e50f50bbe8c4")),
        new TestCustomerId(UUID.fromString("2914cfb6-54ab-42a2-94e1-288ea05701ac")),
        List.of(new TestOrderItemId(UUID.fromString("e5105167-d782-479c-a68a-bc1ac5d27cc4"))),
        new TestMoney(new BigDecimal("1000000"), Currency.getInstance("VND")));
  }
}
