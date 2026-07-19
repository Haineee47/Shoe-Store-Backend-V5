package com.shoestore.shared.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class HttpRequestLoggingFilterTest {

    private ApplicationLogger logger;
    private HttpRequestLoggingFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        logger = mock(ApplicationLogger.class);
        filter = new HttpRequestLoggingFilter(logger);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldLogSuccessfulRequestAtInfoLevel() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/products");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        response.setStatus(200);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(logger).info(messageCaptor.capture());
        verify(logger, never()).error(
                org.mockito.ArgumentMatchers.anyString()
        );

        assertThat(messageCaptor.getValue())
                .startsWith(
                        "HTTP request completed method=GET "
                                + "path=/api/products status=200 durationMs="
                );
    }

    @Test
    void shouldLogClientErrorAtInfoLevel() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/orders");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        response.setStatus(400);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(logger).info(messageCaptor.capture());
        verify(logger, never()).error(
                org.mockito.ArgumentMatchers.anyString()
        );

        assertThat(messageCaptor.getValue())
                .startsWith(
                        "HTTP request completed method=POST "
                                + "path=/api/orders status=400 durationMs="
                );
    }

    @Test
    void shouldLogServerErrorAtErrorLevel() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/products");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        response.setStatus(500);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(logger).error(messageCaptor.capture());
        verify(logger, never()).info(messageCaptor.capture());

        assertThat(messageCaptor.getValue())
                .startsWith(
                        "HTTP request failed method=GET "
                                + "path=/api/products status=500 durationMs="
                );
    }

    @Test
    void shouldExcludeQueryParametersFromLog() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/products");

        request.setQueryString("token=secret&name=shoe");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        response.setStatus(200);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(logger).info(messageCaptor.capture());

        assertThat(messageCaptor.getValue())
                .contains("path=/api/products")
                .doesNotContain("token")
                .doesNotContain("secret")
                .doesNotContain("name=shoe");
    }

    @Test
    void shouldLogEvenWhenFilterChainThrowsException() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/products");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        response.setStatus(500);

        org.mockito.Mockito.doThrow(
                new RuntimeException("Unexpected failure")
        ).when(filterChain).doFilter(request, response);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> filter.doFilter(request, response, filterChain)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Unexpected failure");

        verify(logger).error(
                org.mockito.ArgumentMatchers.startsWith(
                        "HTTP request failed method=GET "
                                + "path=/api/products status=500 durationMs="
                )
        );
    }

    @Test
    void shouldRemoveContextPathFromLoggedPath() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/shoe-store/api/products");

        request.setContextPath("/shoe-store");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        response.setStatus(200);

        filter.doFilter(request, response, filterChain);

        verify(logger).info(
                org.mockito.ArgumentMatchers.startsWith(
                        "HTTP request completed method=GET "
                                + "path=/api/products status=200 durationMs="
                )
        );
    }
}
