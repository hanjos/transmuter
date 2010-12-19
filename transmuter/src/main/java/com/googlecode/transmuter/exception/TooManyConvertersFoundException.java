package com.googlecode.transmuter.exception;

import java.util.Collections;
import java.util.List;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;


/**
 * Thrown when more than one compatible converter for the given converter type was found, and there is no way to decide 
 * which should be used.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class TooManyConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private ConverterType converterType;
  private List<? extends Converter> converters;
  
  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param converters the compatible converters found.
   */
  @SuppressWarnings("unchecked")
  public TooManyConvertersFoundException(ConverterType converterType, List<? extends Converter> converters) {
    super("too many converters found for " + converterType + ": " + converters);
    
    this.converterType = converterType;
    this.converters = converters != null ? Collections.unmodifiableList(converters) : Collections.EMPTY_LIST;
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
   * Returns the compatible converters found.
   * 
   * @return the compatible converters found.
   */
  public List<? extends Converter> getConverters() {
    return converters;
  }
}
