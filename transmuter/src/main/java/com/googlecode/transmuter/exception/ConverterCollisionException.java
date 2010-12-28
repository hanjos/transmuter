package com.googlecode.transmuter.exception;

import static com.googlecode.transmuter.util.ObjectUtils.isEmpty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.googlecode.transmuter.Transmuter;
import com.googlecode.transmuter.converter.Converter;
import com.googlecode.transmuter.converter.ConverterType;


/**
 * Thrown when an attempt to {@link Transmuter#register(Iterable) register} a converter fails due to the presence of 
 * another converter with the same registered type.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private List<? extends Converter> converters;
  private ConverterType converterType;
  
  private static String buildMessage(ConverterType converterType, Converter... converters) {
    return "more than one converter for " + converterType + ": " 
         + ((converters != null) ? Arrays.toString(converters) : "null");
  }
  
  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param converters the conflicting converters.
   */
  @SuppressWarnings("unchecked")
  public ConverterCollisionException(ConverterType converterType, Converter... converters) {
    super(buildMessage(converterType, converters));
    
    this.converters = Collections.unmodifiableList(
        (! isEmpty(converters)) ? Arrays.asList(converters) : Collections.EMPTY_LIST);
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
  public List<? extends Converter> getConverters() {
    return converters;
  }
}
