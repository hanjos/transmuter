package com.googlecode.transmuter.util.exception;

import com.googlecode.transmuter.util.Notification;

/**
 * Thrown when a method expecting a {@link Notification} instance doesn't get one.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class NotificationNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public NotificationNotFoundException() {
    // empty block
  }

  public NotificationNotFoundException(String message) {
    super(message);
  }

  public NotificationNotFoundException(Throwable cause) {
    super(cause);
  }

  public NotificationNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
