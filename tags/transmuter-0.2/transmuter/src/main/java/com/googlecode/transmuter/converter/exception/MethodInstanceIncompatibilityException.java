package com.googlecode.transmuter.converter.exception;

import java.lang.reflect.Method;

/**
 * Thrown when the given method cannot be invoked on the given instance.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class MethodInstanceIncompatibilityException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(Object instance, Method method) {
    return "The method " + method + " cannot be invoked on " + instance;
  }

  private final Object instance;
  private final Method method;
  
  /**
   * Builds a new instance.
   * 
   * @param instance the given instance.
   * @param method the given method.
   */
  public MethodInstanceIncompatibilityException(Object instance, Method method) {
    super(buildMessage(instance, method));
    
    this.instance = instance;
    this.method = method;
  }

  /**
   * Returns the given instance.
   * 
   * @return the given instance.
   */
  public Object getInstance() {
    return instance;
  }

  /**
   * Returns the given method.
   * 
   * @return the given method.
   */
  public Method getMethod() {
    return method;
  }
}
