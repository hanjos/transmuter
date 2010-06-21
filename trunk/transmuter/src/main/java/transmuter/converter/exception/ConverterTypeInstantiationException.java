package transmuter.converter.exception;

import java.util.List;

import transmuter.converter.ConverterType;
import transmuter.exception.MultipleCausesException;

/**
 * Thrown when an attempt to create a new {@link ConverterType} instance fails, bundling the causes.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterTypeInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public ConverterTypeInstantiationException(Exception... causes) {
    super(causes);
  }
  
  public ConverterTypeInstantiationException(List<? extends Exception> causes) {
    super(causes);
  }
  
}