package com.googlecode.transmuter.converter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.googlecode.transmuter.util.ReflectionUtils;


/**
 * Thrown when the return type of a would-be converter method is considered invalid.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class InvalidReturnTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static Type getReturnTypeOf(Method method) {
    if(method == null)
      return null;
    
    return method.getGenericReturnType();
  }
  
  private Method method;
  private Type type;

  /**
   * @param method the would-be converter method.
   */
  public InvalidReturnTypeException(Method method) {
    super(getReturnTypeOf(method) + " is an invalid return type in " + ReflectionUtils.simpleMethodToString(method));
    
    this.method = method;
    this.type = getReturnTypeOf(method);
  }

  /**
   * @return the would-be converter method.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return {@code method}'s (invalid) return type.
   */
  public Type getType() {
    return type;
  }
}
