package com.googlecode.transmuter.type.exception;

/**
 * Thrown if the given base class has no accessible type parameter.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class MissingTypeParameterException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private Class<?> baseClass;
  
  /**
   * @param baseClass the base class.
   */
  public MissingTypeParameterException(Class<?> baseClass) {
    super(baseClass + " has no accessible type parameter!");
    
    this.baseClass = baseClass;
  }

  /**
   * @return the base class.
   */
  public Class<?> getBaseClass() {
    return baseClass;
  }
}
