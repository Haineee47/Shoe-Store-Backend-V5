package com.shoestore.shared.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HttpRequestLogMessageTest {

    @Test
    void shouldFormatCompletedRequestMessage() {
        HttpRequestLogMessage message = new HttpRequestLogMessage(
                "GET",
                "/api/products",
                200,
                15
        );

        assertThat(message.format())
                .isEqualTo(
                        "HTTP request completed method=GET "
                                + "path=/api/products status=200 durationMs=15"
                );

        assertThat(message.isServerError()).isFalse();
    }

    @Test
    void shouldFormatClientErrorAsCompletedRequest() {
        HttpRequestLogMessage message = new HttpRequestLogMessage(
                "POST",
                "/api/orders",
                400,
                10
        );

        assertThat(message.format())
                .isEqualTo(
                        "HTTP request completed method=POST "
                                + "path=/api/orders status=400 durationMs=10"
                );

        assertThat(message.isServerError()).isFalse();
    }

    @Test
    void shouldFormatServerErrorAsFailedRequest() {
        HttpRequestLogMessage message = new HttpRequestLogMessage(
                "GET",
                "/api/products",
                500,
                20
        );

        assertThat(message.format())
                .isEqualTo(
                        "HTTP request failed method=GET "
                                + "path=/api/products status=500 durationMs=20"
                );

        assertThat(message.isServerError()).isTrue();
    }

    @Test
    void shouldRejectNullMethod() {
        assertThatNullPointerException()
                .isThrownBy(() -> new HttpRequestLogMessage(
                        null,
                        "/api/products",
                        200,
                        10
                ))
                .withMessage("method must not be null");
    }

    @Test
    void shouldRejectBlankMethod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HttpRequestLogMessage(
                        " ",
                        "/api/products",
                        200,
                        10
                ))
                .withMessage("method must not be blank");
    }

    @Test
    void shouldRejectNullPath() {
        assertThatNullPointerException()
                .isThrownBy(() -> new HttpRequestLogMessage(
                        "GET",
                        null,
                        200,
                        10
                ))
                .withMessage("path must not be null");
    }

    @Test
    void shouldRejectBlankPath() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HttpRequestLogMessage(
                        "GET",
                        " ",
                        200,
                        10
                ))
                .withMessage("path must not be blank");
    }

    @Test
    void shouldRejectInvalidStatus() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HttpRequestLogMessage(
                        "GET",
                        "/api/products",
                        99,
                        10
                ))
                .withMessage("status must be between 100 and 599");
    }

    @Test
    void shouldRejectNegativeDuration() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HttpRequestLogMessage(
                        "GET",
                        "/api/products",
                        200,
                        -1
                ))
                .withMessage("durationMs must not be negative");
    }
}
