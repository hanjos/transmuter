package com.googlecode.transmuter.converter.exception;

import com.googlecode.transmuter.converter.Binding;

/**
 * Thrown when an {@link Binding#invoke(Object...) invoke} operation fails, bundling the cause. 
 * 
 * @author Humberto S. N. dos Anjos
 */
public class InvocationException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final Binding binding;

  /**
   * Builds a new instance.
   * 
   * @param binding the binding whose invocation failed.  
   */
  public InvocationException(Binding binding) {
    this(binding, null);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param binding the binding whose invocation failed.
   * @param cause the cause of the failure.   
   */
  public InvocationException(Binding binding, Throwable cause) {
    super(cause != null ? cause.getMessage() : null, cause);
    
    this.binding = binding;
  }

  /**
   * Returns the binding whose invocation failed.
   * 
   * @return the binding whose invocation failed.
   */
  public Binding getBinding() {
    return binding;
  }
}
