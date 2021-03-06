package com.googlecode.transmuter.converter.exception;

import com.googlecode.transmuter.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;


/**
 * Thrown when the parameter type of a would-be converter method is considered invalid.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class InvalidParameterTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static Type getParameterTypeOf(Method method) {
    if(method == null || method.getParameterTypes().length != 1)
      return null;
    
    return method.getGenericParameterTypes()[0];
  }
  
  private final Method method;
  private final Type type;
  
  /**
   * Builds a new instance.
   * 
   * @param method the would-be converter method.
   */
  public InvalidParameterTypeException(Method method) {
    super(getParameterTypeOf(method) + " is an invalid parameter type in " + ReflectionUtils.simpleMethodToString(method));
    
    this.method = method;
    this.type = getParameterTypeOf(method);
  }

  /**
   * Returns the would-be converter method.
   * 
   * @return the would-be converter method.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Returns {@code method}'s (invalid) parameter type.
   * 
   * @return {@code method}'s (invalid) parameter type.
   */
  public Type getType() {
    return type;
  }
}
