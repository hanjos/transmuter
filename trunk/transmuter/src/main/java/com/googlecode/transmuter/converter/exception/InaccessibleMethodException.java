package com.googlecode.transmuter.converter.exception;

import java.lang.reflect.Method;

/**
 * Thrown when the method given to a {@link Binding} constructor cannot be externally accessed.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class InaccessibleMethodException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Method method;

  public InaccessibleMethodException(Method method) {
    super(method + " cannot be externally accessed");
    
    this.method = method;
  }

  /**
   * @return the method.
   */
  public Method getMethod() {
    return method;
  }
}
