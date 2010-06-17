package transmuter.exception;

import transmuter.Binding;
import transmuter.ConverterType;

public class ConverterTypeIncompatibleWithBindingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(ConverterType converterType, Binding binding) {
    return binding + " is not compatible with " + converterType;
  }
  
  private ConverterType converterType;
  private Binding binding;
  
  public ConverterTypeIncompatibleWithBindingException(ConverterType converterType, Binding binding) {
    this(converterType, binding, buildMessage(converterType, binding));
  }

  public ConverterTypeIncompatibleWithBindingException(ConverterType converterType, Binding binding, String message) {
    super(message);
    
    this.converterType = converterType;
    this.binding = binding;
  }

  public ConverterType getConverterType() {
    return converterType;
  }

  public Binding getBinding() {
    return binding;
  }
}
