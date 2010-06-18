package transmuter.converter.exception;

import transmuter.converter.Binding;

/**
 * Thrown when an {@link Binding#invoke(Object...) invoke} operation fails, bundling the cause. 
 * 
 * @author Humberto S. N. dos Anjos
 */
public class BindingInvocationException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Binding binding;

  /**
   * @param binding the binding whose invocation failed.  
   */
  public BindingInvocationException(Binding binding) {
    this(binding, null);
  }
  
  /**
   * @param binding the binding whose invocation failed.
   * @param cause the cause of the failure.   
   */
  public BindingInvocationException(Binding binding, Throwable cause) {
    super(cause != null ? cause.getMessage() : null, cause);
    
    this.binding = binding;
  }

  /**
   * @return the binding whose invocation failed.
   */
  public Binding getBinding() {
    return binding;
  }
}
