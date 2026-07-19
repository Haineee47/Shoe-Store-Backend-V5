package com.shoestore.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Logs the completion status and processing duration of every HTTP request.
 *
 * <p>The filter intentionally excludes request bodies, response bodies,
 * headers and query parameters to prevent sensitive information from being
 * written to application logs.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private final ApplicationLogger logger;

    public HttpRequestLoggingFilter() {
        this.logger = ApplicationLoggerFactory.getLogger(
                HttpRequestLoggingFilter.class
        );
    }

    HttpRequestLoggingFilter(ApplicationLogger logger) {
        this.logger = logger;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationNanos = System.nanoTime() - startedAt;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

            HttpRequestLogMessage logMessage = new HttpRequestLogMessage(
                    request.getMethod(),
                    resolvePath(request),
                    response.getStatus(),
                    durationMs
            );

            writeLog(logMessage);
        }
    }

    private String resolvePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath == null || contextPath.isBlank()) {
            return requestUri;
        }

        String path = requestUri.substring(contextPath.length());

        return path.isBlank() ? "/" : path;
    }

    private void writeLog(HttpRequestLogMessage logMessage) {
        if (logMessage.isServerError()) {
            logger.error(logMessage.format());
            return;
        }

        logger.info(logMessage.format());
    }
}
