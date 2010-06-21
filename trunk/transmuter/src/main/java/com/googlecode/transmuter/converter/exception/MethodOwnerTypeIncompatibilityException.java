package com.googlecode.transmuter.converter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Thrown when the given type (dubbed owner type) is not in the given method's declaring class hierarchy.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class MethodOwnerTypeIncompatibilityException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(Method method, Type ownerType) {
    return "The method " + method + " is not compatible with " + ownerType;
  }
  
  private Method method;
  private Type ownerType;

  /**
   * @param method the method.
   * @param the would-be owner type.
   */
  public MethodOwnerTypeIncompatibilityException(Method method, Type ownerType) {
    super(buildMessage(method, ownerType));
    
    this.method = method;
    this.ownerType = ownerType;
  }

  /**
   * @return the method.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return the would-be owner type.
   */
  public Type getOwnerType() {
    return ownerType;
  }
}
