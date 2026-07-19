package com.shoestore.shared.logging;

import java.util.Objects;

/**
 * Represents the information written to the application log after an HTTP
 * request has completed.
 *
 * @param method HTTP request method
 * @param path request path without query parameters
 * @param status HTTP response status
 * @param durationMs request processing duration in milliseconds
 */
public record HttpRequestLogMessage(
        String method,
        String path,
        int status,
        long durationMs
) {

    private static final String COMPLETED_TEMPLATE =
            "HTTP request completed method=%s path=%s status=%d durationMs=%d";

    private static final String FAILED_TEMPLATE =
            "HTTP request failed method=%s path=%s status=%d durationMs=%d";

    public HttpRequestLogMessage {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(path, "path must not be null");

        if (method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }

        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }

        if (status < 100 || status > 599) {
            throw new IllegalArgumentException(
                    "status must be between 100 and 599"
            );
        }

        if (durationMs < 0) {
            throw new IllegalArgumentException(
                    "durationMs must not be negative"
            );
        }
    }

    /**
     * Returns the appropriate log message based on the HTTP status.
     *
     * @return formatted request log message
     */
    public String format() {
        String template = status >= 500
                ? FAILED_TEMPLATE
                : COMPLETED_TEMPLATE;

        return template.formatted(method, path, status, durationMs);
    }

    /**
     * Determines whether the request ended with a server-side failure.
     *
     * @return {@code true} when the status is 500 or greater
     */
    public boolean isServerError() {
        return status >= 500;
    }
}
