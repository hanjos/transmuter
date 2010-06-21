package com.googlecode.transmuter.type.exception;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when an unexpected type is received.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class UnexpectedTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(Type type, Type... expected) {
    StringBuilder message = new StringBuilder();
    
    if(expected == null || expected.length == 0) {
      message.append("Expected nothing, ");
    } else {
      message.append("Expected one of: ");
    
      for (Type clazz : expected) {
        message
          .append((clazz instanceof Class<?>)
            ? ((Class<?>) clazz).getName() 
            : clazz)
          .append(", ");
      }
      
    }
    
    if(type != null)
      message.append("but got: ").append(type.getClass().getName())
          .append(", for type: ").append(type).append('.');
    else
      message.append("but got a null type.");

    return message.toString();
  }
  
  private Type type;
  private List<Type> expected;

  /**
   * @param type the unexpected type.
   * @param expected the expected types.
   */
  public UnexpectedTypeException(Type type, Type... expected) {
    super(buildMessage(type, expected));
    
    this.type = type;
    this.expected = Collections.unmodifiableList(Arrays.asList(expected));
  }

  /**
   * @return the unexpected type.
   */
  public Type getType() {
    return type;
  }

  /**
   * @return the expected types.
   */
  public List<Type> getExpected() {
    return expected;
  }
}
