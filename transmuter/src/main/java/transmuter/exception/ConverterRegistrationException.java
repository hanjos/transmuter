package transmuter.exception;

import java.util.List;

public class ConverterRegistrationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public ConverterRegistrationException(Exception... causes) {
    super(causes);
  }
  
  public ConverterRegistrationException(List<? extends Exception> causes) {
    super(causes);
  }
}
