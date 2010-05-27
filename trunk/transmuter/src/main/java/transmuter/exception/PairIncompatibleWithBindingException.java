package transmuter.exception;

import transmuter.util.Binding;
import transmuter.util.Pair;

public class PairIncompatibleWithBindingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(Pair pair, Binding binding) {
    return binding + " is not compatible with " + pair;
  }
  
  private Pair pair;
  private Binding binding;
  
  public PairIncompatibleWithBindingException(Pair pair, Binding binding) {
    this(pair, binding, buildMessage(pair, binding));
  }

  public PairIncompatibleWithBindingException(Pair pair, Binding binding, String message) {
    super(message);
    
    this.pair = pair;
    this.binding = binding;
  }

  public Pair getPair() {
    return pair;
  }

  public Binding getBinding() {
    return binding;
  }
}
