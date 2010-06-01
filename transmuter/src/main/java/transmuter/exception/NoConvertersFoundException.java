package transmuter.exception;

import transmuter.util.Pair;

public class NoConvertersFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Pair pair;

  public NoConvertersFoundException(Pair pair) {
    super("No converters found for " + pair);
    
    this.pair = pair;
  }

  public Pair getPair() {
    return pair;
  }
}
