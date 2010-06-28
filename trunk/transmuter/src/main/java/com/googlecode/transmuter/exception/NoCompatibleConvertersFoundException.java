package com.googlecode.transmuter.exception;

import com.googlecode.transmuter.converter.ConverterType;

/**
 * Thrown when no compatible converter for the given converter type was found.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class NoCompatibleConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private ConverterType converterType;

  /**
   * @param converterType the converter type.
   */
  public NoCompatibleConvertersFoundException(ConverterType converterType) {
    super("no compatible converters found for " + converterType);
    
    this.converterType = converterType;
  }

  /**
   * Returns the converter type.
   * 
   * @return the converter type.
   */
  public ConverterType getConverterType() {
    return converterType;
  }
}
