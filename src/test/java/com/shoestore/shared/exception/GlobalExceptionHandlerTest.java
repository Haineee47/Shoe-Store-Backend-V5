package com.shoestore.shared.exception;

import com.shoestore.shared.logging.ApplicationLogger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private static final String EXCEPTION_LOG_TEMPLATE =
            "Exception handled type={} errorCode={} method={} path={} status={}";

    private ApplicationLogger logger;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        logger = mock(ApplicationLogger.class);

        GlobalExceptionHandler exceptionHandler =
                new GlobalExceptionHandler(logger);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void shouldReturnValidationErrorResponseAndLogWarning()
            throws Exception {

        mockMvc.perform(
                        post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Request validation failed")
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("VALIDATION_FAILED")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/validation")
                )
                .andExpect(
                        jsonPath("$.errors.name")
                                .value("Name must not be blank")
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(logger).warn(
                eq(EXCEPTION_LOG_TEMPLATE),
                eq("MethodArgumentNotValidException"),
                eq("VALIDATION_FAILED"),
                eq("POST"),
                eq("/test/validation"),
                eq(400)
        );

        verify(logger, never()).error(
                anyString(),
                any(Throwable.class)
        );
    }

    @Test
    void shouldReturnMalformedRequestResponseAndLogWarning()
            throws Exception {

        mockMvc.perform(
                        post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name":
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request body is missing or malformed"
                                )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("MALFORMED_REQUEST_BODY")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/validation")
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(logger).warn(
                eq(EXCEPTION_LOG_TEMPLATE),
                eq("HttpMessageNotReadableException"),
                eq("MALFORMED_REQUEST_BODY"),
                eq("POST"),
                eq("/test/validation"),
                eq(400)
        );

        verify(logger, never()).error(
                anyString(),
                any(Throwable.class)
        );
    }

    @Test
    void shouldReturnMethodNotAllowedResponseAndLogWarning()
            throws Exception {

        mockMvc.perform(delete("/test/validation"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(
                        jsonPath("$.error")
                                .value("Method Not Allowed")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "HTTP method is not supported "
                                                + "for this endpoint"
                                )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("METHOD_NOT_ALLOWED")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/validation")
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(logger).warn(
                eq(EXCEPTION_LOG_TEMPLATE),
                eq("HttpRequestMethodNotSupportedException"),
                eq("METHOD_NOT_ALLOWED"),
                eq("DELETE"),
                eq("/test/validation"),
                eq(405)
        );

        verify(logger, never()).error(
                anyString(),
                any(Throwable.class)
        );
    }

    @Test
    void shouldReturnApplicationErrorResponseAndLogWarning()
            throws Exception {

        mockMvc.perform(get("/test/application-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Test application failure")
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("TEST_APPLICATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/application-exception")
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(logger).warn(
                eq(EXCEPTION_LOG_TEMPLATE),
                eq("ApplicationException"),
                eq("TEST_APPLICATION_ERROR"),
                eq("GET"),
                eq("/test/application-exception"),
                eq(400)
        );

        verify(logger, never()).error(
                anyString(),
                any(Throwable.class)
        );
    }

    @Test
    void shouldReturnServerApplicationErrorAndLogStackTrace()
            throws Exception {

        mockMvc.perform(get("/test/server-application-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.error")
                                .value("Internal Server Error")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Test server application failure")
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("TEST_SERVER_ERROR")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/test/server-application-exception"
                                )
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(logger).error(
                eq(EXCEPTION_LOG_TEMPLATE),
                eq("ApplicationException"),
                eq("TEST_SERVER_ERROR"),
                eq("GET"),
                eq("/test/server-application-exception"),
                eq(500),
                any(ApplicationException.class)
        );

        verify(logger, never()).warn(
                anyString(),
                any(Object[].class)
        );
    }

    @Test
    void shouldReturnInternalServerErrorWithoutExposingExceptionMessage()
            throws Exception {

        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.error")
                                .value("Internal Server Error")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("An unexpected error occurred")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers.not(
                                                "Sensitive database detail"
                                        )
                                )
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value("INTERNAL_SERVER_ERROR")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/unexpected")
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(logger).error(
                eq(EXCEPTION_LOG_TEMPLATE),
                eq("IllegalStateException"),
                eq("INTERNAL_SERVER_ERROR"),
                eq("GET"),
                eq("/test/unexpected"),
                eq(500),
                any(IllegalStateException.class)
        );

        verify(logger, never()).warn(
                anyString(),
                any(Object[].class)
        );
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        void validateRequest(
                @Valid @RequestBody TestRequest request
        ) {
            // No implementation is required for this test endpoint.
        }

        @GetMapping("/application-exception")
        void throwApplicationException() {
            throw new ApplicationException(
                    TestErrorCode.APPLICATION_ERROR
            );
        }

        @GetMapping("/server-application-exception")
        void throwServerApplicationException() {
            throw new ApplicationException(
                    TestErrorCode.SERVER_ERROR
            );
        }

        @GetMapping("/unexpected")
        void throwUnexpectedException() {
            throw new IllegalStateException(
                    "Sensitive database detail"
            );
        }
    }

    record TestRequest(
            @NotBlank(message = "Name must not be blank")
            String name
    ) {
    }

    enum TestErrorCode implements ErrorCode {

        APPLICATION_ERROR(
                "TEST_APPLICATION_ERROR",
                "Test application failure",
                HttpStatus.BAD_REQUEST
        ),

        SERVER_ERROR(
                "TEST_SERVER_ERROR",
                "Test server application failure",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        private final String code;
        private final String message;
        private final HttpStatus httpStatus;

        TestErrorCode(
                String code,
                String message,
                HttpStatus httpStatus
        ) {
            this.code = code;
            this.message = message;
            this.httpStatus = httpStatus;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public HttpStatus getHttpStatus() {
            return httpStatus;
        }
    }
}

