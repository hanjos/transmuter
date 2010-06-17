package transmuter.exception;

import java.util.Collections;
import java.util.List;

import transmuter.converter.Binding;
import transmuter.converter.ConverterType;

public class TooManyConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private ConverterType converterType;
  private List<Binding> bindings;
  
  @SuppressWarnings("unchecked")
  public TooManyConvertersFoundException(ConverterType converterType, List<Binding> bindings) {
    super("too many converters found for " + converterType + ": " + bindings);
    
    this.converterType = converterType;
    this.bindings = bindings != null ? Collections.unmodifiableList(bindings) : Collections.EMPTY_LIST;
  }

  public ConverterType getConverterType() {
    return converterType;
  }

  public List<Binding> getBindings() {
    return bindings;
  }
}
