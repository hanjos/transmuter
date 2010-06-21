package com.googlecode.transmuter.converter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.googlecode.transmuter.util.ReflectionUtils;


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
  
  private Method method;
  private Type type;
  
  /**
   * @param method the would-be converter method.
   */
  public InvalidParameterTypeException(Method method) {
    super(getParameterTypeOf(method) + " is an invalid parameter type in " + ReflectionUtils.simpleMethodToString(method));
    
    this.method = method;
    this.type = getParameterTypeOf(method);
  }

  /**
   * @return the would-be converter method.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return {@code method}'s (invalid) parameter type.
   */
  public Type getType() {
    return type;
  }
}
