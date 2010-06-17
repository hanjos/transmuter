package transmuter.converter.exception;

import java.util.List;

import transmuter.exception.MultipleCausesException;

public class ConverterTypeInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public ConverterTypeInstantiationException(Exception... causes) {
    super(causes);
  }
  
  public ConverterTypeInstantiationException(List<? extends Exception> causes) {
    super(causes);
  }
  
}
