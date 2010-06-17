package transmuter.exception;

import transmuter.ConverterType;

public class NoCompatibleConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private ConverterType converterType;

  public NoCompatibleConvertersFoundException(ConverterType converterType) {
    super("no compatible converters found for " + converterType);
    
    this.converterType = converterType;
  }

  public ConverterType getConverterType() {
    return converterType;
  }
}
