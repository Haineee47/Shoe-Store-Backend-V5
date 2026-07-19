package com.shoestore.shared.exception;

import java.util.Objects;

/**
 * Base unchecked exception for expected application failures.
 *
 * <p>This exception carries a structured {@link ErrorCode} that is
 * translated into the standardized API error response.</p>
 */
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Creates an exception using the default message of the error code.
     */
    public ApplicationException(ErrorCode errorCode) {
        super(requireErrorCode(errorCode).getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Creates an exception using a contextual message while preserving
     * the stable machine-readable error code.
     */
    public ApplicationException(
            ErrorCode errorCode,
            String message
    ) {
        super(requireMessage(message));
        this.errorCode = requireErrorCode(errorCode);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(
                errorCode,
                "errorCode must not be null"
        );
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
