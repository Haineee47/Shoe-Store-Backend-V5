package com.shoestore.shared.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationLoggerFactoryTest {

    @Test
    void shouldCreateApplicationLogger() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertNotNull(logger);
    }

    @Test
    void shouldLogInfoWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.info("Information message"));
    }

    @Test
    void shouldLogInfoWithArgumentsWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.info("User {} logged in", "admin"));
    }

    @Test
    void shouldLogWarningWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.warn("Warning message"));
    }

    @Test
    void shouldLogWarningWithArgumentsWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.warn("Inventory is below threshold: {}", 5));
    }

    @Test
    void shouldLogDebugWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.debug("Debug message"));
    }

    @Test
    void shouldLogDebugWithArgumentsWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.debug("Processing order {}", 1001));
    }

    @Test
    void shouldLogErrorWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() ->
                logger.error("Error message"));
    }

    @Test
    void shouldLogErrorWithExceptionWithoutThrowingException() {

        ApplicationLogger logger = ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        RuntimeException exception = new RuntimeException("Test exception");

        assertDoesNotThrow(() -> logger.error("Unexpected error occurred", exception));
    }

    @Test
    void shouldLogErrorWithArgumentsWithoutThrowingException() {

        ApplicationLogger logger = ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        assertDoesNotThrow(() -> logger.error(
                "Exception handled type={} errorCode={} status={}",
                "BusinessException",
                "USR_001",
                400));
    }

    @Test
    void shouldLogErrorWithArgumentsAndThrowableWithoutThrowingException() {

        ApplicationLogger logger =
                ApplicationLoggerFactory.getLogger(ApplicationLoggerFactoryTest.class);

        RuntimeException exception =
                new RuntimeException("Test exception");

        assertDoesNotThrow(() ->
                logger.error(
                        "Exception handled type={} errorCode={} status={}",
                        "BusinessException",
                        "USR_001",
                        400,
                        exception));
    }
}
