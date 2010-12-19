package com.googlecode.transmuter.converter.exception;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;

/**
 * Thrown when the given converter type is not compatible with the one extracted from the given converter.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterTypeIncompatibleWithConverterException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(ConverterType converterType, Converter converter) {
    return converter + " is not compatible with " + converterType;
  }
  
  private ConverterType converterType;
  private Converter converter;
  
  /**
   * Builds a new instance.
   * 
   * @param converterType a converter type.
   * @param converter a converter.
   */
  public ConverterTypeIncompatibleWithConverterException(ConverterType converterType, Converter converter) {
    super(buildMessage(converterType, converter));
    
    this.converterType = converterType;
    this.converter = converter;
  }

  /**
   * Returns the converter type.
   * 
   * @return the converter type.
   */
  public ConverterType getConverterType() {
    return converterType;
  }

  /**
   * Returns the converter.
   * 
   * @return the converter.
   */
  public Converter getConverter() {
    return converter;
  }
}
