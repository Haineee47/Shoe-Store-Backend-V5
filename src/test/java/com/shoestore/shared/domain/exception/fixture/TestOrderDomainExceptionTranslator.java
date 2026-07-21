package com.shoestore.shared.domain.exception.fixture;

import java.util.Objects;

/**
 * Test-only application-boundary translator for order-domain failures.
 *
 * <p>This translator is intentionally explicit. It does not map error
 * codes by enum name, reflection, HTTP status, or a generic fallback.</p>
 */
public final class TestOrderDomainExceptionTranslator {

    public TestApplicationException translate(
            TestDomainException exception
    ) {
        Objects.requireNonNull(
                exception,
                "exception must not be null"
        );

        TestDomainErrorCode domainErrorCode =
                exception.errorCode();

        if (!(domainErrorCode instanceof TestOrderErrorCode orderErrorCode)) {
            throw new IllegalArgumentException(
                    "Unsupported domain error-code type: "
                            + domainErrorCode.getClass().getName()
            );
        }

        return switch (orderErrorCode) {
            case ORDER_HAS_NO_ITEMS,
                 ORDER_CANNOT_BE_CONFIRMED ->
                    new TestApplicationException(
                            TestApplicationErrorCode
                                    .ORDER_CONFIRMATION_REJECTED,
                            "Order confirmation was rejected",
                            exception
                    );

            case ORDER_CANNOT_BE_CANCELLED ->
                    new TestApplicationException(
                            TestApplicationErrorCode
                                    .ORDER_CANCELLATION_REJECTED,
                            "Order cancellation was rejected",
                            exception
                    );

            case ORDER_ALREADY_CONFIRMED,
                 ORDER_CANNOT_BE_MODIFIED,
                 ORDER_ITEM_QUANTITY_MUST_BE_POSITIVE ->
                    new TestApplicationException(
                            TestApplicationErrorCode
                                    .ORDER_MODIFICATION_REJECTED,
                            "Order modification was rejected",
                            exception
                    );

            case ORDER_CANNOT_BE_COMPLETED ->
                    throw new IllegalStateException(
                            "No application mapping defined for domain error: "
                                    + orderErrorCode
                    );
        };
    }
}
