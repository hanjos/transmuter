package transmuter.exception;

import java.util.Collections;
import java.util.List;

import transmuter.util.Binding;
import transmuter.util.Pair;

public class TooManyConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Pair pair;
  private List<Binding> bindings;
  
  public TooManyConvertersFoundException(Pair pair, List<Binding> bindings) {
    super("too many converters found for " + pair + ": " + bindings);
    
    this.pair = pair;
    this.bindings = bindings != null ? Collections.unmodifiableList(bindings) : Collections.EMPTY_LIST;
  }

  public Pair getPair() {
    return pair;
  }

  public List<Binding> getBindings() {
    return bindings;
  }
}
