package transmuter.exception;

import static transmuter.util.ObjectUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import transmuter.converter.Binding;
import transmuter.converter.ConverterType;

public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private List<Binding> bindings;
  private ConverterType converterType;
  
  private static String buildMessage(ConverterType converterType, Binding... bindings) {
    return "more than one converter for " + converterType + ": " 
         + ((bindings != null) ? Arrays.toString(bindings) : "null");
  }
  
  @SuppressWarnings("unchecked")
  public ConverterCollisionException(ConverterType converterType, Binding... bindings) {
    super(buildMessage(converterType, bindings));
    
    this.bindings = Collections.unmodifiableList(
        (! isEmpty(bindings)) ? Arrays.asList(bindings) : Collections.EMPTY_LIST);
    this.converterType = converterType;
  }

  public ConverterType getConverterType() {
    return converterType;
  }

  public List<Binding> getBindings() {
    return bindings;
  }
}
