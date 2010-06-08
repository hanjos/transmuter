package transmuter.exception;

import static transmuter.util.ObjectUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import transmuter.util.Binding;
import transmuter.util.Pair;

public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private List<Binding> bindings;
  private Pair pair;
  
  private static String buildMessage(Pair pair, Binding... bindings) {
    return "more than one converter for " + pair + ": " 
         + ((bindings != null) ? Arrays.toString(bindings) : "null");
  }
  
  @SuppressWarnings("unchecked")
  public ConverterCollisionException(Pair pair, Binding... bindings) {
    super(buildMessage(pair, bindings));
    
    this.bindings = Collections.unmodifiableList(
        (! isEmpty(bindings)) ? Arrays.asList(bindings) : Collections.EMPTY_LIST);
    this.pair = pair;
  }

  public Pair getPair() {
    return pair;
  }

  public List<Binding> getBindings() {
    return bindings;
  }
}
