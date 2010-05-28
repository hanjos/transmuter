package transmuter.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MultipleCausesException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static final List<Exception> EMPTY_EXCEPTION_LIST = Arrays.asList(new Exception[0]);
  
  private static String buildMessage(List<? extends Exception> causes) {
    if(causes.isEmpty())
      return "";
    
    final int length = causes.size();
    
    StringBuilder sb = new StringBuilder("Multiple exceptions found:\n    ").append(causes.get(0));
    
    for(int i = 1; i < length; i++)
      sb.append(";\n    ").append(causes.get(i));
    
    return sb.toString();
  }

  private List<? extends Exception> causes;
  
  public MultipleCausesException(Exception... causes) {
    this((causes != null) ? Arrays.asList(causes) : EMPTY_EXCEPTION_LIST);
  }
  
  public MultipleCausesException(List<? extends Exception> causes) {
    super(buildMessage((causes != null) ? causes : EMPTY_EXCEPTION_LIST));
    
    this.causes = Collections.unmodifiableList((causes != null) ? causes : EMPTY_EXCEPTION_LIST);
  }

  public List<? extends Exception> getCauses() {
    return causes;
  }
  
  @Override
  public Throwable getCause() {
    return null;
  }
}
