package com.shoestore.shared.logging;

/**
 * Application logging abstraction.
 *
 * <p>Business code should depend on this interface instead of the underlying logging framework.
 */
public interface ApplicationLogger {

  void info(String message);

  void info(String message, Object... arguments);

  void warn(String message);

  void warn(String message, Object... arguments);

  void error(String message);

  void error(String message, Throwable throwable);

  void error(String message, Object... arguments);

  void debug(String message);

  void debug(String message, Object... arguments);
}
