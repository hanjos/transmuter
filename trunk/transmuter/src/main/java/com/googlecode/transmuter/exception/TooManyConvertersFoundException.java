package com.googlecode.transmuter.exception;

import java.util.Collections;
import java.util.List;

import com.googlecode.transmuter.converter.Binding;
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
  private List<Binding> bindings;
  
  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param bindings the compatible bindings found.
   */
  @SuppressWarnings("unchecked")
  public TooManyConvertersFoundException(ConverterType converterType, List<Binding> bindings) {
    super("too many converters found for " + converterType + ": " + bindings);
    
    this.converterType = converterType;
    this.bindings = bindings != null ? Collections.unmodifiableList(bindings) : Collections.EMPTY_LIST;
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
   * Returns the compatible bindings found.
   * 
   * @return the compatible bindings found.
   */
  public List<Binding> getBindings() {
    return bindings;
  }
}