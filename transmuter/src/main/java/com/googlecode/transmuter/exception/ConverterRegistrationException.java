package com.googlecode.transmuter.exception;

import java.util.Collection;

import com.googlecode.transmuter.core.Transmuter;
import com.googlecode.transmuter.util.exception.MultipleCausesException;

/**
 * Thrown by the {@link Transmuter#register(Iterable) register} operation on failure, bundling the causes. 
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
  public ConverterRegistrationException(Collection<? extends Exception> causes) {
    super(causes);
  }
}
