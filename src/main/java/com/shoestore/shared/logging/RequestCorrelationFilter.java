package com.shoestore.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures that every HTTP request has a unique request identifier.
 *
 * <p>The request identifier is added to the SLF4J MDC so that all log entries produced while
 * processing the request can include the same correlation value.
 */
@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestId = resolveRequestId(request);

    MDC.put(LoggingConstants.REQUEST_ID_MDC_KEY, requestId);
    response.setHeader(LoggingConstants.REQUEST_ID_HEADER, requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(LoggingConstants.REQUEST_ID_MDC_KEY);
    }
  }

  private String resolveRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(LoggingConstants.REQUEST_ID_HEADER);

    if (requestId == null || requestId.isBlank()) {
      return UUID.randomUUID().toString();
    }

    return requestId;
  }
}
