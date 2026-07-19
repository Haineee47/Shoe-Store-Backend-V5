package com.shoestore.shared.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessfulResponseWithData() {
        Map<String, String> data = Map.of(
                "id", "product-001"
        );

        ApiResponse<Map<String, String>> response =
                ApiResponse.success(data);

        assertThat(response.success()).isTrue();
        assertThat(response.message())
                .isEqualTo("Request completed successfully");
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void shouldCreateSuccessfulResponseWithCustomMessageAndData() {
        String data = "created";

        ApiResponse<String> response =
                ApiResponse.success("Resource created successfully", data);

        assertThat(response.success()).isTrue();
        assertThat(response.message())
                .isEqualTo("Resource created successfully");
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void shouldCreateSuccessfulResponseWithoutData() {
        ApiResponse<Void> response =
                ApiResponse.success("Resource deleted successfully");

        assertThat(response.success()).isTrue();
        assertThat(response.message())
                .isEqualTo("Resource deleted successfully");
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isNotNull();
    }
}
