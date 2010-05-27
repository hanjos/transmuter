package transmuter.util.exception;

import transmuter.util.Binding;

public class BindingInvocationException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Binding binding;

  public BindingInvocationException(Binding binding) {
    this(binding, null);
  }
  
  public BindingInvocationException(Binding binding, Throwable cause) {
    super(cause);
    
    this.binding = binding;
  }

  public Binding getBinding() {
    return binding;
  }
}
