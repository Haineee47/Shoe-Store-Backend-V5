package com.shoestore.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter =
            new RequestCorrelationFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldUseRequestIdProvidedByClient() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                LoggingConstants.REQUEST_ID_HEADER,
                "client-request-id"
        );

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            assertEquals(
                    "client-request-id",
                    MDC.get(LoggingConstants.REQUEST_ID_MDC_KEY)
            );
        };

        filter.doFilter(request, response, filterChain);

        assertEquals(
                "client-request-id",
                response.getHeader(LoggingConstants.REQUEST_ID_HEADER)
        );
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            String requestId =
                    MDC.get(LoggingConstants.REQUEST_ID_MDC_KEY);

            assertFalse(requestId.isBlank());
        };

        filter.doFilter(request, response, filterChain);

        String responseRequestId =
                response.getHeader(LoggingConstants.REQUEST_ID_HEADER);

        assertTrue(response.containsHeader(
                LoggingConstants.REQUEST_ID_HEADER
        ));

        assertFalse(responseRequestId.isBlank());
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderIsBlank() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                LoggingConstants.REQUEST_ID_HEADER,
                "   "
        );

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            String requestId =
                    MDC.get(LoggingConstants.REQUEST_ID_MDC_KEY);

            assertFalse(requestId.isBlank());
        };

        filter.doFilter(request, response, filterChain);

        String responseRequestId =
                response.getHeader(LoggingConstants.REQUEST_ID_HEADER);

        assertFalse(responseRequestId.isBlank());
        assertFalse(responseRequestId.equals("   "));
    }

    @Test
    void shouldExposeSameRequestIdInMdcAndResponse() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            String mdcRequestId =
                    MDC.get(LoggingConstants.REQUEST_ID_MDC_KEY);

            String responseRequestId =
                    response.getHeader(
                            LoggingConstants.REQUEST_ID_HEADER
                    );

            assertEquals(mdcRequestId, responseRequestId);
        };

        filter.doFilter(request, response, filterChain);
    }

    @Test
    void shouldRemoveRequestIdFromMdcAfterRequestCompletes()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            assertTrue(MDC.get(
                    LoggingConstants.REQUEST_ID_MDC_KEY
            ) != null);
        };

        filter.doFilter(request, response, filterChain);

        assertNull(
                MDC.get(LoggingConstants.REQUEST_ID_MDC_KEY)
        );
    }

    @Test
    void shouldRemoveRequestIdWhenFilterChainThrowsException() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            throw new ServletException("Test failure");
        };

        try {
            filter.doFilter(request, response, filterChain);
        } catch (Exception ignored) {
            // Expected test exception.
        }

        assertNull(
                MDC.get(LoggingConstants.REQUEST_ID_MDC_KEY)
        );
    }
}
