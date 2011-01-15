package com.googlecode.transmuter.util.exception;

import java.util.Arrays;
import java.util.Collection;

import com.googlecode.transmuter.util.StringUtils;

/**
 * Thrown on an error while building an object (using a constructor, factory method or otherwise).
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ObjectInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  private Class<?> objectType;
  
  private static String buildMessage(Class<?> objectType, Collection<? extends Exception> causes) {
    String msg = "Error while instantiating " + objectType;
    
    if(causes == null || causes.isEmpty())
      return msg;
    
    return msg + ":\n    " + StringUtils.concatenate(";\n    ", causes);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param objectType the failed constructor's type. 
   * @param causes the exceptions to be bundled. 
   */
  public ObjectInstantiationException(Class<?> objectType, Exception... causes) {
    this(objectType, (causes != null) ? Arrays.asList(causes) : EMPTY_EXCEPTION_LIST);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param objectType the failed constructor's type. 
   * @param causes the exceptions to be bundled. 
   */
  public ObjectInstantiationException(Class<?> objectType, Collection<? extends Exception> causes) {
    super(buildMessage(objectType, causes), causes);
    
    this.objectType = objectType;
  }

  /**
   * Returns the failed constructor's type.
   * 
   * @return the failed constructor's type.
   */
  public Class<?> getObjectType() {
    return objectType;
  }
}
