package com.shoestore.shared.logging;

import org.slf4j.Logger;

class Slf4jApplicationLogger implements ApplicationLogger {

  private final Logger logger;

  Slf4jApplicationLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void info(String message, Object... arguments) {
    logger.info(message, arguments);
  }

  @Override
  public void warn(String message) {
    logger.warn(message);
  }

  @Override
  public void warn(String message, Object... arguments) {
    logger.warn(message, arguments);
  }

  @Override
  public void error(String message) {
    logger.error(message);
  }

  @Override
  public void error(String message, Throwable throwable) {
    logger.error(message, throwable);
  }

  @Override
  public void error(String message, Object... arguments) {
    logger.error(message, arguments);
  }

  @Override
  public void debug(String message) {
    logger.debug(message);
  }

  @Override
  public void debug(String message, Object... arguments) {
    logger.debug(message, arguments);
  }
}
