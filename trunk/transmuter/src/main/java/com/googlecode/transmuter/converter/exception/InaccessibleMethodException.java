package com.googlecode.transmuter.converter.exception;

import com.googlecode.transmuter.converter.Binding;

import java.lang.reflect.Method;

/**
 * Thrown when the method given to a {@link Binding} constructor cannot be externally accessed.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class InaccessibleMethodException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final Method method;

  /**
   * Builds a new instance.
   * 
   * @param method the flawed method.
   */
  public InaccessibleMethodException(Method method) {
    super(method + " cannot be externally accessed");
    
    this.method = method;
  }

  /**
   * Returns the inaccessible method.
   * 
   * @return the inaccessible method.
   */
  public Method getMethod() {
    return method;
  }
}
