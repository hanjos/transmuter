package transmuter.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConverterRegistrationException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private List<? extends Exception> causes;
  
  public ConverterRegistrationException(Exception... causes) {
    this(Arrays.asList(causes));
  }
  
  public ConverterRegistrationException(List<? extends Exception> causes) {
    this.causes = Collections.unmodifiableList(causes);
  }

  public List<? extends Exception> getCauses() {
    return causes;
  }
  
  @Override
  public Throwable getCause() {
    return (causes != null && causes.size() > 0) ? causes.get(0) : null; 
  }
}
