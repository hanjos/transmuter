package transmuter.exception;

import transmuter.util.Pair;

public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Pair pair;
  
  private static String buildMessage(Pair pair) {
    return "More than one converter for " + pair;
  }
  
  public ConverterCollisionException(Pair pair) {
    this(pair, buildMessage(pair));
  }

  public ConverterCollisionException(Pair pair, String message) {
    super(message);
    
    this.pair = pair;
  }

  public Pair getPair() {
    return pair;
  }
}
