package transmuter.exception;

import java.util.List;

public class PairInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public PairInstantiationException(Exception... causes) {
    super(causes);
  }
  
  public PairInstantiationException(List<? extends Exception> causes) {
    super(causes);
  }
  
}
