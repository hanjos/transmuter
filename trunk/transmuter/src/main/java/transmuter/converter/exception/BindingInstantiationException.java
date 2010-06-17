package transmuter.converter.exception;

import java.util.List;

import transmuter.exception.MultipleCausesException;

public class BindingInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public BindingInstantiationException(Exception... causes) {
    super(causes);
  }

  public BindingInstantiationException(List<? extends Exception> causes) {
    super(causes);
  }
}
