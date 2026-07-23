package com.shoestore.shared.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventMetadataTest {

  private static final UUID EVENT_ID = UUID.fromString("f39c9351-ad08-4503-a15c-bc546c17fb26");

  private static final Instant OCCURRED_AT = Instant.parse("2026-07-22T04:45:00Z");

  @Test
  void shouldPreserveExplicitMetadata() {
    DomainEventMetadata metadata = new DomainEventMetadata(EVENT_ID, OCCURRED_AT);

    assertThat(metadata.eventId()).isEqualTo(EVENT_ID);

    assertThat(metadata.occurredAt()).isEqualTo(OCCURRED_AT);
  }

  @Test
  void shouldUseValueSemantics() {
    DomainEventMetadata first = new DomainEventMetadata(EVENT_ID, OCCURRED_AT);

    DomainEventMetadata second = new DomainEventMetadata(EVENT_ID, OCCURRED_AT);

    assertThat(first).isEqualTo(second);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void shouldRejectNullEventId() {
    assertThatThrownBy(() -> new DomainEventMetadata(null, OCCURRED_AT))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("eventId must not be null");
  }

  @Test
  void shouldRejectNullOccurredAt() {
    assertThatThrownBy(() -> new DomainEventMetadata(EVENT_ID, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("occurredAt must not be null");
  }
}
