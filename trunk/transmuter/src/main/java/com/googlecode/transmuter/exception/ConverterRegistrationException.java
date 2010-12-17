package com.googlecode.transmuter.exception;

import java.util.List;

import com.googlecode.transmuter.Transmuter;
import com.googlecode.transmuter.util.exception.MultipleCausesException;

/**
 * Thrown by the {@link Transmuter#register(Object) register} operation on failure, bundling the causes. 
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterRegistrationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  /**
   * Builds a new instance.
   * 
   * @param causes the exceptions to be bundled.
   */
  public ConverterRegistrationException(Exception... causes) {
    super(causes);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param causes the exceptions to be bundled.
   */
  public ConverterRegistrationException(List<? extends Exception> causes) {
    super(causes);
  }
}
