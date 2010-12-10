package com.googlecode.transmuter.exception;

import static com.googlecode.transmuter.util.ObjectUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.googlecode.transmuter.Transmuter;
import com.googlecode.transmuter.converter.Binding;
import com.googlecode.transmuter.converter.ConverterType;


/**
 * Thrown when an attempt to {@link Transmuter#register(Object) register} a converter fails due to the presence of 
 * another converter with the same registered type.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private List<? extends Binding> bindings;
  private ConverterType converterType;
  
  private static String buildMessage(ConverterType converterType, Binding... bindings) {
    return "more than one converter for " + converterType + ": " 
         + ((bindings != null) ? Arrays.toString(bindings) : "null");
  }
  
  /**
   * Builds a new instance.
   * 
   * @param converterType the converter type.
   * @param bindings the conflicting bindings.
   */
  @SuppressWarnings("unchecked")
  public ConverterCollisionException(ConverterType converterType, Binding... bindings) {
    super(buildMessage(converterType, bindings));
    
    this.bindings = Collections.unmodifiableList(
        (! isEmpty(bindings)) ? Arrays.asList(bindings) : Collections.EMPTY_LIST);
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
   * Returns a list with the conflicting bindings.
   * 
   * @return a list with the conflicting bindings.
   */
  public List<? extends Binding> getBindings() {
    return bindings;
  }
}
