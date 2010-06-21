package com.googlecode.transmuter.converter.exception;

import com.googlecode.transmuter.converter.Binding;
import com.googlecode.transmuter.converter.ConverterType;

/**
 * Thrown when the given converter type is not compatible with the one extracted from the given binding.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterTypeIncompatibleWithBindingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(ConverterType converterType, Binding binding) {
    return binding + " is not compatible with " + converterType;
  }
  
  private ConverterType converterType;
  private Binding binding;
  
  public ConverterTypeIncompatibleWithBindingException(ConverterType converterType, Binding binding) {
    super(buildMessage(converterType, binding));
    
    this.converterType = converterType;
    this.binding = binding;
  }

  /**
   * @return the converter type.
   */
  public ConverterType getConverterType() {
    return converterType;
  }

  /**
   * @return the binding.
   */
  public Binding getBinding() {
    return binding;
  }
}
