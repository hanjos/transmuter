package transmuter.exception;

import java.util.List;

public class TypeExtractionException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public TypeExtractionException(Exception... causes) {
    super(causes);
  }
  
  public TypeExtractionException(List<? extends Exception> causes) {
    super(causes);
  }
  
}
