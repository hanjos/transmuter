package transmuter.converter.exception;

import java.util.List;

import transmuter.exception.MultipleCausesException;

/**
 * Thrown when an attempt to create a new {@link Binding} instance fails, bundling the causes.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class BindingInstantiationException extends MultipleCausesException {
  private static final long serialVersionUID = 1L;

  public BindingInstantiationException(Exception... causes) {
    super(causes);
  }

  public BindingInstantiationException(List<? extends Exception> causes) {
    super(causes);
  }
}
