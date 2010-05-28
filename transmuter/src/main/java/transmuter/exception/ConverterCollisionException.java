package transmuter.exception;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import transmuter.util.Pair;

public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private List<Method> methods;
  private Pair pair;
  
  private static String buildMessage(List<Method> methods, Pair pair) {
    return "more than one converter for " + pair + ": " + methods;
  }
  
  public ConverterCollisionException(List<Method> methods, Pair pair) {
    this(methods, pair, buildMessage(methods, pair));
  }

  public ConverterCollisionException(List<Method> methods, Pair pair, String message) {
    super(message);
    
    this.methods = Collections.unmodifiableList(
        (methods != null) ? methods : Collections.EMPTY_LIST);
    this.pair = pair;
  }

  public Pair getPair() {
    return pair;
  }

  public List<Method> getMethods() {
    return methods;
  }
}
