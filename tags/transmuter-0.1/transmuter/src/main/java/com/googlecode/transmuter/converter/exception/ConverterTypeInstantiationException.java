package com.googlecode.transmuter.converter.exception;

import java.util.List;

import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.exception.MultipleCausesException;


/**
 * Thrown when an attempt to create a new {@link ConverterType} instance fails, bundling the causes.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterTypeInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  /**
   * Builds a new instance.
   * 
   * @param causes the exceptions to be bundled.
   */
  public ConverterTypeInstantiationException(Exception... causes) {
    super(causes);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param causes the exceptions to be bundled.
   */
  public ConverterTypeInstantiationException(List<? extends Exception> causes) {
    super(causes);
  }
  
}
