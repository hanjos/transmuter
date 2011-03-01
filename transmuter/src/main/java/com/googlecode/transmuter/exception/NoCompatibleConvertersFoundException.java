package com.googlecode.transmuter.exception;

import java.util.Collection;
import java.util.Collections;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;

/**
 * Thrown when no compatible converter for the given converter type was found.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class NoCompatibleConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final ConverterType converterType;
  private final Iterable<? extends Converter> converters;

  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param converters the converters checked.
   */
  public NoCompatibleConvertersFoundException(ConverterType converterType, Collection<? extends Converter> converters) {
    super("No compatible converters found for " + converterType + " in " + converters);
    
    this.converterType = converterType;
    this.converters = (converters == null) ? null : Collections.unmodifiableCollection(converters);
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
   * Returns the converters checked.
   * 
   * @return the converters checked.
   */
  public Iterable<? extends Converter> getConverters() {
    return converters;
  }
}
