package com.googlecode.transmuter.exception;

import static com.googlecode.transmuter.util.ObjectUtils.isEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;
import com.googlecode.transmuter.core.Transmuter;

/**
 * Thrown when an attempt to {@link Transmuter#register(Iterable) register} a converter fails due to the presence of 
 * another converter with the same registered type.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final Collection<? extends Converter> converters;
  private final ConverterType converterType;
  
  private static String buildMessage(ConverterType converterType, Collection<? extends Converter> converters) {
    return "More than one converter for " + converterType + ": " + converters;
  }
  
  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param converters the conflicting converters.
   */
  @SuppressWarnings("unchecked")
  public ConverterCollisionException(ConverterType converterType, Converter... converters) {
    this(converterType, (! isEmpty(converters)) ? Arrays.asList(converters) : Collections.EMPTY_LIST);
  }
  
  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param converters the conflicting converters.
   */
  public ConverterCollisionException(ConverterType converterType, Collection<? extends Converter> converters) {
    super(buildMessage(converterType, converters));
    
    this.converters = Collections.unmodifiableCollection(converters);
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

  /**
   * Returns a list with the conflicting converters.
   * 
   * @return a list with the conflicting converters.
   */
  public Collection<? extends Converter> getConverters() {
    return converters;
  }
}
