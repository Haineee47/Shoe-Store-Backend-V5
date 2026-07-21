package com.shoestore.shared.domain.exception;

import org.junit.jupiter.api.Test;

import com.shoestore.shared.domain.exception.fixture.TestDomainException;
import com.shoestore.shared.domain.exception.fixture.TestOrder;
import com.shoestore.shared.domain.exception.fixture.TestOrderErrorCode;
import com.shoestore.shared.domain.exception.fixture.TestOrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class InvalidStateTransitionConventionTest {

    @Test
    void shouldConfirmDraftOrderWithItems() {
        TestOrder order = TestOrder.createDraft();
        order.addItem(1);

        order.confirm();

        assertThat(order.status())
                .isSameAs(TestOrderStatus.CONFIRMED);
    }

    @Test
    void shouldCompleteConfirmedOrder() {
        TestOrder order = confirmedOrder();

        order.complete();

        assertThat(order.status())
                .isSameAs(TestOrderStatus.COMPLETED);
    }

    @Test
    void shouldCancelDraftOrder() {
        TestOrder order = TestOrder.createDraft();

        order.cancel();

        assertThat(order.status())
                .isSameAs(TestOrderStatus.CANCELLED);
    }

    @Test
    void shouldRejectConfirmationOfCancelledOrder() {
        TestOrder order = TestOrder.createDraft();
        order.cancel();

        TestDomainException exception = catchThrowableOfType(
                order::confirm,
                TestDomainException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.errorCode())
                .isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_CONFIRMED);
        assertThat(exception)
                .hasMessage("Only a draft order can be confirmed");
    }

    @Test
    void shouldPreserveCancelledStateWhenConfirmationIsRejected() {
        TestOrder order = TestOrder.createDraft();
        order.cancel();

        catchThrowableOfType(
                order::confirm,
                TestDomainException.class
        );

        assertThat(order.status())
                .isSameAs(TestOrderStatus.CANCELLED);
    }

    @Test
    void shouldRejectConfirmationOfCompletedOrder() {
        TestOrder order = completedOrder();

        TestDomainException exception = catchThrowableOfType(
                order::confirm,
                TestDomainException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.errorCode())
                .isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_CONFIRMED);
        assertThat(order.status())
                .isSameAs(TestOrderStatus.COMPLETED);
    }

    @Test
    void shouldRejectCancellationOfConfirmedOrder() {
        TestOrder order = confirmedOrder();

        TestDomainException exception = catchThrowableOfType(
                order::cancel,
                TestDomainException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.errorCode())
                .isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED);
        assertThat(exception)
                .hasMessage("Only a draft order can be cancelled");
    }

    @Test
    void shouldPreserveConfirmedStateWhenCancellationIsRejected() {
        TestOrder order = confirmedOrder();

        catchThrowableOfType(
                order::cancel,
                TestDomainException.class
        );

        assertThat(order.status())
                .isSameAs(TestOrderStatus.CONFIRMED);
    }

    @Test
    void shouldRejectCancellationOfCompletedOrder() {
        TestOrder order = completedOrder();

        TestDomainException exception = catchThrowableOfType(
                order::cancel,
                TestDomainException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.errorCode())
                .isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_CANCELLED);
        assertThat(order.status())
                .isSameAs(TestOrderStatus.COMPLETED);
    }

    @Test
    void shouldRejectCompletionOfDraftOrder() {
        TestOrder order = TestOrder.createDraft();

        TestDomainException exception = catchThrowableOfType(
                order::complete,
                TestDomainException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.errorCode())
                .isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_COMPLETED);
        assertThat(exception)
                .hasMessage("Only a confirmed order can be completed");
    }

    @Test
    void shouldPreserveDraftStateWhenCompletionIsRejected() {
        TestOrder order = TestOrder.createDraft();

        catchThrowableOfType(
                order::complete,
                TestDomainException.class
        );

        assertThat(order.status())
                .isSameAs(TestOrderStatus.DRAFT);
    }

    @Test
    void shouldRejectCompletionOfCancelledOrder() {
        TestOrder order = TestOrder.createDraft();
        order.cancel();

        TestDomainException exception = catchThrowableOfType(
                order::complete,
                TestDomainException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.errorCode())
                .isSameAs(TestOrderErrorCode.ORDER_CANNOT_BE_COMPLETED);
        assertThat(order.status())
                .isSameAs(TestOrderStatus.CANCELLED);
    }

    @Test
    void shouldTreatRepeatedCancellationAsIdempotent() {
        TestOrder order = TestOrder.createDraft();
        order.cancel();

        order.cancel();

        assertThat(order.status())
                .isSameAs(TestOrderStatus.CANCELLED);
    }

    @Test
    void shouldTreatRepeatedCompletionAsIdempotent() {
        TestOrder order = completedOrder();

        order.complete();

        assertThat(order.status())
                .isSameAs(TestOrderStatus.COMPLETED);
    }

    private static TestOrder confirmedOrder() {
        TestOrder order = TestOrder.createDraft();
        order.addItem(1);
        order.confirm();
        return order;
    }

    private static TestOrder completedOrder() {
        TestOrder order = confirmedOrder();
        order.complete();
        return order;
    }
}
