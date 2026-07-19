package com.shoestore.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard response structure for successful API operations.
 *
 * @param success   indicates whether the request was successful
 * @param message   human-readable response message
 * @param data      response payload
 * @param timestamp time when the response was created
 * @param <T>       response payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {

    /**
     * Creates a successful response containing data.
     *
     * @param data response payload
     * @param <T>  response payload type
     * @return standardized successful response
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                "Request completed successfully",
                data,
                Instant.now()
        );
    }

    /**
     * Creates a successful response with a custom message and data.
     *
     * @param message response message
     * @param data    response payload
     * @param <T>     response payload type
     * @return standardized successful response
     */
    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                Instant.now()
        );
    }

    /**
     * Creates a successful response without a data payload.
     *
     * @param message response message
     * @return standardized successful response
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(
                true,
                message,
                null,
                Instant.now()
        );
    }
}
