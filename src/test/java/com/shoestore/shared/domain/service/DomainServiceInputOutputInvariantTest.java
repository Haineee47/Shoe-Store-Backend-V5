package com.shoestore.shared.domain.service;

import com.shoestore.shared.domain.service.fixture.TestAllocationDecision;
import com.shoestore.shared.domain.service.fixture.TestAllocationPolicy;
import com.shoestore.shared.domain.service.fixture.TestAllocationQuantity;
import com.shoestore.shared.domain.service.fixture.TestAllocationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainServiceInputOutputInvariantTest {

    private final TestAllocationPolicy allocationPolicy =
            new TestAllocationPolicy();

    @Test
    void shouldRejectNullRequestedQuantity() {
        TestAllocationQuantity availableQuantity =
                new TestAllocationQuantity(10);

        assertThatThrownBy(() ->
                allocationPolicy.allocate(
                        null,
                        availableQuantity
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("requestedQuantity must not be null");
    }

    @Test
    void shouldRejectNullAvailableQuantity() {
        TestAllocationQuantity requestedQuantity =
                new TestAllocationQuantity(5);

        assertThatThrownBy(() ->
                allocationPolicy.allocate(
                        requestedQuantity,
                        null
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("availableQuantity must not be null");
    }

    @Test
    void shouldRejectZeroRequestedQuantity() {
        TestAllocationQuantity requestedQuantity =
                new TestAllocationQuantity(0);

        TestAllocationQuantity availableQuantity =
                new TestAllocationQuantity(10);

        assertThatThrownBy(() ->
                allocationPolicy.allocate(
                        requestedQuantity,
                        availableQuantity
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "requestedQuantity must be greater than zero"
                );
    }

    @Test
    void shouldAcceptZeroAvailableQuantity() {
        TestAllocationDecision decision =
                allocationPolicy.allocate(
                        new TestAllocationQuantity(5),
                        new TestAllocationQuantity(0)
                );

        assertThat(decision.allocatedQuantity())
                .isEqualTo(new TestAllocationQuantity(0));

        assertThat(decision.remainingAvailableQuantity())
                .isEqualTo(new TestAllocationQuantity(0));

        assertThat(decision.unfulfilledQuantity())
                .isEqualTo(new TestAllocationQuantity(5));

        assertThat(decision.status())
                .isEqualTo(TestAllocationStatus.NOT_ALLOCATED);
    }

    @Test
    void shouldReturnNonNullDecisionComponents() {
        TestAllocationDecision decision =
                allocationPolicy.allocate(
                        new TestAllocationQuantity(8),
                        new TestAllocationQuantity(5)
                );

        assertThat(decision.allocatedQuantity())
                .isNotNull();

        assertThat(decision.remainingAvailableQuantity())
                .isNotNull();

        assertThat(decision.unfulfilledQuantity())
                .isNotNull();

        assertThat(decision.status())
                .isNotNull();
    }

    @Test
    void shouldReturnValueBasedImmutableDecision() {
        TestAllocationDecision firstDecision =
                allocationPolicy.allocate(
                        new TestAllocationQuantity(8),
                        new TestAllocationQuantity(5)
                );

        TestAllocationDecision secondDecision =
                allocationPolicy.allocate(
                        new TestAllocationQuantity(8),
                        new TestAllocationQuantity(5)
                );

        assertThat(firstDecision)
                .isEqualTo(secondDecision);

        assertThat(firstDecision.hashCode())
                .isEqualTo(secondDecision.hashCode());

        assertThat(firstDecision)
                .isNotSameAs(secondDecision);
    }

    @Test
    void shouldRejectNegativeAllocationQuantity() {
        assertThatThrownBy(() ->
                new TestAllocationQuantity(-1)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "allocation quantity must not be negative"
                );
    }

    @Test
    void shouldRejectNullAllocatedQuantityInDecision() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        null,
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(5),
                        TestAllocationStatus.NOT_ALLOCATED
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "allocatedQuantity must not be null"
                );
    }

    @Test
    void shouldRejectNullRemainingAvailableQuantityInDecision() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(0),
                        null,
                        new TestAllocationQuantity(5),
                        TestAllocationStatus.NOT_ALLOCATED
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "remainingAvailableQuantity must not be null"
                );
    }

    @Test
    void shouldRejectNullUnfulfilledQuantityInDecision() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(0),
                        null,
                        TestAllocationStatus.NOT_ALLOCATED
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "unfulfilledQuantity must not be null"
                );
    }

    @Test
    void shouldRejectNullStatusInDecision() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(5),
                        null
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status must not be null");
    }

    @Test
    void shouldRejectFullyAllocatedDecisionWithUnfulfilledQuantity() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(4),
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(1),
                        TestAllocationStatus.FULLY_ALLOCATED
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "fully allocated decision must have zero unfulfilled quantity"
                );
    }

    @Test
    void shouldRejectPartiallyAllocatedDecisionWithoutAllocatedQuantity() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(5),
                        TestAllocationStatus.PARTIALLY_ALLOCATED
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "partially allocated decision must have allocated and unfulfilled quantities"
                );
    }

    @Test
    void shouldRejectPartiallyAllocatedDecisionWithoutUnfulfilledQuantity() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(5),
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(0),
                        TestAllocationStatus.PARTIALLY_ALLOCATED
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "partially allocated decision must have allocated and unfulfilled quantities"
                );
    }

    @Test
    void shouldRejectNotAllocatedDecisionWithAllocatedQuantity() {
        assertThatThrownBy(() -> new TestAllocationDecision(
                new TestAllocationQuantity(1),
                new TestAllocationQuantity(0),
                new TestAllocationQuantity(4),
                TestAllocationStatus.NOT_ALLOCATED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "not allocated decision must have zero allocated quantity");
    }

    @Test
    void shouldRejectFullyAllocatedDecisionWithoutAllocatedQuantity() {
        assertThatThrownBy(() ->
                new TestAllocationDecision(
                        new TestAllocationQuantity(0),
                        new TestAllocationQuantity(5),
                        new TestAllocationQuantity(0),
                        TestAllocationStatus.FULLY_ALLOCATED
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "fully allocated decision must have allocated quantity"
                );
    }

    @Test
    void shouldRejectNotAllocatedDecisionWithoutUnfulfilledQuantity() {
        assertThatThrownBy(() -> new TestAllocationDecision(
                new TestAllocationQuantity(0),
                new TestAllocationQuantity(5),
                new TestAllocationQuantity(0),
                TestAllocationStatus.NOT_ALLOCATED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "not allocated decision must have unfulfilled quantity");
    }

    @Test
    void shouldReturnIndependentDecisionInstances() {
        TestAllocationDecision first = allocationPolicy.allocate(
                new TestAllocationQuantity(6),
                new TestAllocationQuantity(4));

        TestAllocationDecision second = allocationPolicy.allocate(
                new TestAllocationQuantity(6),
                new TestAllocationQuantity(4));

        assertThat(first)
                .isEqualTo(second);

        assertThat(first)
                .isNotSameAs(second);
    }

    @Test
    void shouldLeaveInputValuesUnchanged() {
        TestAllocationQuantity requested =
                new TestAllocationQuantity(9);

        TestAllocationQuantity available =
                new TestAllocationQuantity(4);

        allocationPolicy.allocate(requested, available);

        assertThat(requested)
                .isEqualTo(new TestAllocationQuantity(9));

        assertThat(available)
                .isEqualTo(new TestAllocationQuantity(4));
    }
}
