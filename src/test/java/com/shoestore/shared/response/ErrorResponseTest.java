package com.shoestore.shared.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void shouldCreateStandardErrorResponse() {
        ErrorResponse response = ErrorResponse.of(
                404,
                "Not Found",
                "Resource not found",
                "RESOURCE_NOT_FOUND",
                "/api/v1/resources/123"
        );

        assertThat(response.success()).isFalse();
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("Resource not found");
        assertThat(response.errorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.path()).isEqualTo("/api/v1/resources/123");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.errors()).isNull();
    }

    @Test
    void shouldCreateValidationErrorResponse() {
        Map<String, String> fieldErrors = Map.of(
                "name", "Name must not be blank",
                "price", "Price must be greater than zero"
        );

        ErrorResponse response = ErrorResponse.validation(
                400,
                "Bad Request",
                "Request validation failed",
                "VALIDATION_FAILED",
                "/api/v1/products",
                fieldErrors
        );

        assertThat(response.success()).isFalse();
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.message())
                .isEqualTo("Request validation failed");
        assertThat(response.errorCode())
                .isEqualTo("VALIDATION_FAILED");
        assertThat(response.path())
                .isEqualTo("/api/v1/products");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.errors()).isEqualTo(fieldErrors);
    }
}
