package com.shoestore.shared.logging;

/** Factory for creating application loggers. */
public final class ApplicationLoggerFactory {

  private ApplicationLoggerFactory() {}

  public static ApplicationLogger getLogger(Class<?> type) {
    return new Slf4jApplicationLogger(org.slf4j.LoggerFactory.getLogger(type));
  }
}
