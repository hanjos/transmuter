package transmuter.exception;

import java.util.List;

public class PairCreationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public PairCreationException(Exception... causes) {
    super(causes);
  }
  
  public PairCreationException(List<? extends Exception> causes) {
    super(causes);
  }
  
}
