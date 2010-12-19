package com.googlecode.transmuter.util.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.googlecode.transmuter.util.StringUtils;


/**
 * Thrown on an error in an object's constructor.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ObjectInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  private Class<?> objectType;
  private List<?> arguments;
  
  private static String buildMessage(Class<?> objectType, List<?> arguments, List<? extends Exception> causes) {
    String msg = "Error while instantiating " + objectType + " with arguments " + arguments;
    
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
    this(objectType, null, (causes != null) ? Arrays.asList(causes) : EMPTY_EXCEPTION_LIST);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param objectType the failed constructor's type. 
   * @param causes the exceptions to be bundled. 
   */
  public ObjectInstantiationException(Class<?> objectType, List<? extends Exception> causes) {
    this(objectType, null, causes);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param objectType the failed constructor's type. 
   * @param arguments the arguments to the constructor.
   * @param causes the exceptions to be bundled. 
   */
  public ObjectInstantiationException(Class<?> objectType, List<?> arguments, Exception... causes) {
    this(objectType, arguments, (causes != null) ? Arrays.asList(causes) : EMPTY_EXCEPTION_LIST);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param objectType the failed constructor's type. 
   * @param arguments the arguments to the constructor.
   * @param causes the exceptions to be bundled. 
   */
  public ObjectInstantiationException(Class<?> objectType, List<?> arguments, List<? extends Exception> causes) {
    super(buildMessage(objectType, arguments, causes), causes);
    
    this.objectType = objectType;
    this.arguments = Collections.unmodifiableList((arguments != null) ? arguments : Collections.emptyList());
  }

  /**
   * Returns the failed constructor's type.
   * 
   * @return the failed constructor's type.
   */
  public Class<?> getObjectType() {
    return objectType;
  }

  /**
   * Returns the arguments to the constructor.
   * 
   * @return the arguments to the constructor.
   */
  public List<?> getArguments() {
    return arguments;
  }
}
