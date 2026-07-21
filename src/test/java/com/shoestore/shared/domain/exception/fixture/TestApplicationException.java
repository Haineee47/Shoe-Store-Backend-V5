package com.shoestore.shared.domain.exception.fixture;

import java.util.Objects;

/**
 * Test-only application exception produced after translating
 * a known domain rejection at the application boundary.
 */
public final class TestApplicationException extends RuntimeException {

    private final TestApplicationErrorCode errorCode;

    public TestApplicationException(
            TestApplicationErrorCode errorCode,
            String message,
            TestDomainException cause
    ) {
        super(requireMessage(message), Objects.requireNonNull(
                cause,
                "cause must not be null"
        ));

        this.errorCode = Objects.requireNonNull(
                errorCode,
                "errorCode must not be null"
        );
    }

    public TestApplicationErrorCode errorCode() {
        return errorCode;
    }

    private static String requireMessage(String message) {
        Objects.requireNonNull(message, "message must not be null");

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "message must not be blank"
            );
        }

        return message;
    }
}
